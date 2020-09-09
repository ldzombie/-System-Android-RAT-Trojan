package oom.android.system.Managers;

import android.content.pm.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import oom.android.system.app.MyService;

public class AppList {

    public static JSONObject getInstalledApps(boolean getSysPackages) {

        JSONArray apps = new JSONArray();

        List<PackageInfo> packs = MyService.getContext().getPackageManager().getInstalledPackages(0);

        for(int i=0;i < packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            try {
                JSONObject newInfo = new JSONObject();
                String appname = p.applicationInfo.loadLabel(MyService.getContext().getPackageManager()).toString();
                String pname = p.packageName;
                String versionName = p.versionName;
                int versionCode = p.versionCode;

                newInfo.put(base.APPS.APP_NAME,appname);
                newInfo.put(base.APPS.PACKAGE_NAME,pname);
                newInfo.put(base.APPS.VERSION_NAME,versionName);
                newInfo.put(base.APPS.VERSION_CODE,versionCode);
                apps.put(newInfo);
            }catch (JSONException e) {}
        }

        JSONObject data = new JSONObject();
        try {
            data.put(base.APPS.APPS, apps);
        }catch (JSONException e) {}

        return data;
    }


}
