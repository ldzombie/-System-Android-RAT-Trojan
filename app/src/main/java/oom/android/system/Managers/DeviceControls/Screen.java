package oom.android.system.Managers.DeviceControls;

import android.app.WallpaperManager;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import java.io.InputStream;
import java.net.URL;

import oom.android.system.app.MyService;

public class Screen {

    public static void setBrightnessLevel(int value){
        Settings.System.putInt(MyService.getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
    }

    public static void setWallpaper(String path){
        try {
            InputStream ins = new URL(path).openStream();
            WallpaperManager wpm = WallpaperManager.getInstance(MyService.getContext());
            wpm.setStream(ins);
        }catch (Exception e){}
    }

    public static void ScreenLock(){
        PowerManager manager = (PowerManager)MyService.getContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "oom.android.system:wakelock");
        wl.acquire();
        wl.release();
    }
}
