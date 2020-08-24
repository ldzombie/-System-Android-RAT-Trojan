package oom.android.system.Managers.Images;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreManager {

    public static List<JSONObject> getMobImages(Context context) {
        List<JSONObject> ImagesList = new ArrayList<>();
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};

            Cursor cur = context.getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
            while (cur.moveToNext()) {
                ImageBean listImagesBean = new ImageBean();
                listImagesBean.setImageDisplayName(cur.getString(5));
                listImagesBean.setImageId(cur.getString(0));
                listImagesBean.setImageSize(cur.getString(6));
                listImagesBean.setImagePath(cur.getString(3));
                ImagesList.add(listImagesBean.toJSONObject());
            }
            cur.close();

        } catch (Exception e) {
            Log.e("MyLogs", e.toString());
        }
        return ImagesList;
    }

}
