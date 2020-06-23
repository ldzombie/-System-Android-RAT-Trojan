package oom.android.system.Managers.DeviceControls;

import android.app.WallpaperManager;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import java.io.InputStream;
import java.net.URL;

public class Screen {
    Context context;

    public Screen(Context context){
        this.context = context;
    }

    public void setBrightnessLevel(int value){
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
    }

    public void setWallpaper(String path){
        try {
            InputStream ins = new URL(path).openStream();
            WallpaperManager wpm = WallpaperManager.getInstance(context);
            wpm.setStream(ins);
        }catch (Exception e){}
    }

    public void ScreenLock(){
        PowerManager manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "oom.android.system:wakelock");
        wl.acquire();
        wl.release();
    }
}
