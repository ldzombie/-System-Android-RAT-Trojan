package oom.android.system.Managers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oom.android.system.app.MyService;

public class DeviceInfoManager {

    Activity activity;
    private static Context context;

    public DeviceInfoManager(Activity activity){
        try {
            this.context =MyService.getContext();
            this.activity =activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getInfo(){
        try {
            JSONObject info = new JSONObject();
            JSONArray list = new JSONArray();

            String DeviceName = getDeviceName();
            String provider = get_provider();

            info.put("DeviceName", DeviceName);
            info.put("Provider",provider);

            String status="",type="";
            getbattery();
            switch (MyService.battery_status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    if(MyService.charge_type==BatteryManager.BATTERY_PLUGGED_USB){
                        type="usb";
                    }
                    else if(MyService.charge_type==BatteryManager.BATTERY_PLUGGED_AC){
                        type="AC";
                    }
                    status="charging";
                    break;

                case BatteryManager.BATTERY_STATUS_FULL:
                    status="full";
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    status="discharging";
                    break;
            }
            info.put("battery_level",MyService.battery_level+" "+status+" "+type);
            list.put(info);

            info.put("info", list);
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static String get_provider(){
        try{
            TelephonyManager tm =(TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);

            String provider= tm.getSimOperatorName();
            return provider;
        }
        catch(Exception e){
            Log.d(MyService.LOG_TAG, "ошика получения провайдера:\n "+ e.toString());
            return "unknown";
        }
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    static void getbattery(){
        try{
            BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    context.unregisterReceiver(this);
                    int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        level = (rawlevel * 100) / scale;
                    }
                    MyService.battery_level=level;
                    MyService.battery_status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    MyService.charge_type = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                }
            };
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        }
        catch(Exception e){}

    }
}
