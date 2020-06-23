package oom.android.system.Managers.Images;

import android.util.Log;

import org.json.JSONObject;

import java.text.DecimalFormat;

import oom.android.system.Managers.base;

public class ImageBean  {

    private String imageId;
    private String imageDisplayName;
    private String imageSize;
    private String imagePath;


    public void setImageId(String imageId) { this.imageId = imageId; }
    public void setImageDisplayName(String imageDisplayName) { this.imageDisplayName = imageDisplayName; }
    public void setImageSize(String imageSize) {
        final double kilobytes = Double.valueOf(imageSize) / 1024;
        //final double megabytes = kilobytes / 1024;
        String formattedDouble = new DecimalFormat("#000.00").format(kilobytes);
        this.imageSize = formattedDouble;
    }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    protected JSONObject toJSONObject() {
        JSONObject media = new JSONObject();
        try {
            media.put(base.IMAGES.IMAGE_ID, isEmpty(imageId));
            media.put(base.IMAGES.IMAGE_DISPLAY_NAME, isEmpty(imageDisplayName));
            media.put(base.IMAGES.IMAGE_SIZE, isEmpty(imageSize+"KB"));
            media.put(base.IMAGES.IMAGE_PATH, isEmpty(imagePath));
        } catch (Exception e) {
            Log.e("MyLogs", e.toString());
        }
        return media;
    }

    public String isEmpty(String check){
        if(check.length() ==0)
            return "";
        else
            return check;
    }

}
