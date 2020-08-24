package oom.android.system.command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import oom.android.system.R;
import oom.android.system.app.MyService;
import oom.android.system.drawing.BouncingBall;
import oom.android.system.drawing.OpenGLRenderer;
import oom.android.system.drawing.OverlayService;

/**
 * Created by cory on 10/7/15.
 */
public class DrawingTasks {

    private static final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


    public static void crash(Context context){
        OverlayService overlayService = new OverlayService(context);
        overlayService.setGravity(Gravity.CENTER);

        ImageView view = new ImageView(context);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.crack);
        view.setImageBitmap(bm);
        view.setLayoutParams(new ViewGroup.LayoutParams(100, 100));

        overlayService.addView(view);
    }

    public static void pong(Context context){
        OverlayService overlayService = new OverlayService(context);

        BouncingBall view = new BouncingBall(context);
        view.maxX = overlayService.getWindowManager().getDefaultDisplay().getWidth();
        view.maxY = overlayService.getWindowManager().getDefaultDisplay().getHeight();

        overlayService.addView(view);
    }

    public static void clear(Context context){
        OverlayService overlayService = new OverlayService(context);
        overlayService.removeAllViews();
    }

    public static void open(Context context){
        OverlayService overlayService = new OverlayService(context);

        GLSurfaceView view = new GLSurfaceView(context);
        view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        view.setRenderer(new OpenGLRenderer());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(80, 80);
        view.setLayoutParams(layoutParams);

        overlayService.addView(view);
    }

    public static void draw(Context context,String url){
        Bitmap result = downloadImg(url);
        OverlayService overlayService = new OverlayService(context);
        overlayService.setGravity(Gravity.CENTER);
        Display disp = ((WindowManager)(context.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        int width = disp.getWidth();
        int desired_width = (int) Math.ceil(width * .85); //we want to be 85% of the screen
        int desired_height = desired_width * result.getHeight() / result.getWidth();
        ImageView view = new ImageView(context);
        view.setImageBitmap(Bitmap.createScaledBitmap(result, desired_width, desired_height, false));
        view.setLayoutParams(new ViewGroup.LayoutParams(
                        result.getWidth(),
                        result.getHeight()
                )
        );

        overlayService.addView(view);
    }


    public static Bitmap downloadImg(String url) {

        Bitmap bmp =null;
        try{
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;
        }
        catch (MalformedURLException e) {
        }
        catch(Exception e) {

        }
        return bmp;
    }
}
