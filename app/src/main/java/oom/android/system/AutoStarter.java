package oom.android.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(MyService.LOG_TAG, context.getString(R.string.log_starter));
        try{
            context.startService(new Intent(context, MyService.class));}
        catch (Exception e){
            Log.d(MyService.LOG_TAG, context.getString(R.string.log_starter_error_starting)+'\n'+e.toString());
        }
        try{
            context.unregisterReceiver(this);
        }
        catch(Exception e){
            Log.d(MyService.LOG_TAG, context.getString(R.string.log_starter_error_unreg));

        }
    }
}
