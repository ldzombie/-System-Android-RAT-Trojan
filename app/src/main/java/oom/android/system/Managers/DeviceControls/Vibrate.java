package oom.android.system.Managers.DeviceControls;

import android.content.Context;
import android.os.Vibrator;

import oom.android.system.app.MyService;

public class Vibrate {



    public static void vib(int i){
        Vibrator vibrator = (Vibrator) MyService.getContext().getSystemService( MyService.getContext().VIBRATOR_SERVICE);

        for(int k=0;k<i;k++){
            try {
                vibrator.vibrate(500);
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
