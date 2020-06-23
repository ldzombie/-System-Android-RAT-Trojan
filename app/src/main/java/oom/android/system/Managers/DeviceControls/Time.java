package oom.android.system.Managers.DeviceControls;

import android.content.Context;
import android.icu.util.TimeZone;

import java.util.concurrent.ExecutionException;

public class Time {
    Context context;

    public Time(Context context){
        this.context = context;
    }
//> API 24
    public void setTimeZone(String zone){
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                TimeZone tz = TimeZone.getTimeZone(zone);
            }
        }catch (Exception e){}
    }
}
