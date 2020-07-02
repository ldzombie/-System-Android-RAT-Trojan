package oom.android.system.Managers.DeviceControls;

import android.content.Context;
import android.media.AudioManager;

import oom.android.system.app.MyService;

public class Audio {


    public static void setVolume(int stream,int volume){
        AudioManager manager = (AudioManager)MyService.getContext().getSystemService(Context.AUDIO_SERVICE);
        int value =  manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        manager.setStreamVolume(stream,volume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public static Integer getVolume(){
        AudioManager manager = (AudioManager) MyService.getContext().getSystemService(Context.AUDIO_SERVICE);
        int value =  manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return value;

    }

    public static void setMode(int mode){
        AudioManager manager = (AudioManager)MyService.getContext().getSystemService(Context.AUDIO_SERVICE);
        manager.setMode(mode);
    }

    public static Integer getMode(){
        AudioManager manager = (AudioManager)MyService.getContext().getSystemService(Context.AUDIO_SERVICE);
        return manager.getMode();
    }

    public static void playEffect(int effect,int volume){
        AudioManager manager = (AudioManager)MyService.getContext().getSystemService(Context.AUDIO_SERVICE);
        if(volume != 0)
            manager.playSoundEffect(effect,volume);
        else
            manager.playSoundEffect(effect);

    }

}
