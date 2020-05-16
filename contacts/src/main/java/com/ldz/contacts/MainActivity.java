package com.ldz.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final String   TAG = "CONTACT";

    ListView contactList;
    ArrayList<String> contacts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactList = findViewById(R.id.contactList);
        readContacts(this);


    }

    private void readContacts(Context context)
    {
        final Contact contact;
        Cursor cursor=context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if(cursor !=null) {
            contact = new Contact();
            while(cursor.moveToNext()) {

                String id = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts._ID));
                contact.setId(id);

                String name = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts
                                        .DISPLAY_NAME));
                contact.setName(name);

                String has_phone = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts
                                        .HAS_PHONE_NUMBER));
                if (Integer.parseInt(has_phone) > 0) {
                    // extract phone number
                    Cursor pCur;
                    pCur = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds
                                    .Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds
                                    .Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while(pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(
                                        ContactsContract.
                                                CommonDataKinds.
                                                Phone.NUMBER));
                       contact.setPhone(phone);


                    }
                    pCur.close();
                }
                contacts.add(contact.getId()+":"+ contact.getName() + ":"+contact.getPhone());
                Log.d(TAG, "Contact id="  + contact.getId()    + ", name="  + contact.getName () +", phone=" + contact.getPhone());
            }
        }
        // создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, contacts);
        // устанавливаем для списка адаптер
        contactList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String con = contacts.get(position);
                String[] c = con.split(":");
                Log.d("MYLOG",c[0]+" "+c[1]+" "+c[2]);
                UpdateContact(c[0],c[1],c[2],"hello","123 456 78 96");
            }
        });
    }

    public void AddContact(String name, String phone){

        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, new ContentValues());

        long rawContactId =  ContentUris.parseId(rawContactUri);

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
                .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "free directory assistance")
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,name)

                .build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        /*ContentValues values = new ContentValues();

        /* Связываем наш аккаунт с данными
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        /* Устанавливаем MIMETYPE для поля данных
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        /* Имя для нашего аккаунта
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);

        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        /* Тип данных – номер телефона
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        /* Номер телефона
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        /* Тип – мобильный
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);*/


    }

    public void DeleteContact(String name) {

        String[] projection = new String[]{ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,ContactsContract.RawContacts._ID};

        String Raw_id = "";

        Cursor c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,projection,null,null,null);
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
        String[] args = new String[]{name,Raw_id};
        getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,wh, args);
    }

    public  void UpdateContact(String RawId,String name,String phone, String N_name, String N_phone){
        updateP(phone,N_phone);
        updateN(name,N_name);
        //updateR(RawId,name,N_name);

    }

    private  void updateP(String phone,String N_phone){
        try{
            ContentResolver resolver = getContentResolver();

            ContentValues values = new ContentValues();

            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,N_phone);

            String wh=ContactsContract.CommonDataKinds.Phone.NUMBER +"=?";
            String[] args = new String[]{phone};
            resolver.update(ContactsContract.Data.CONTENT_URI,values,wh,args);

        }catch (Exception e){}

    }

    private  void updateN(String name,String N_name){
        try{
            ContentResolver resolver = getContentResolver();

            ContentValues values = new ContentValues();

            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,N_name);
            String wh = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME+"=?";
            String[] args = new String[]{name};

            resolver.update(ContactsContract.Data.CONTENT_URI,values,wh,args);

        }catch (Exception e){}

    }


    private  void updateR(String RawId,String name,String N_name){
        try{
            ContentResolver resolver = getContentResolver();
         /*String[] projection = new String[]{ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,ContactsContract.RawContacts._ID};

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
        c.close();*/

            String wh = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+"=? AND "+ ContactsContract.RawContacts._ID+"=?";
            String[] args = new String[]{name,RawId};
            ContentValues values = new ContentValues();
            values.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, N_name);


            resolver.update(ContactsContract.RawContacts.CONTENT_URI,values,wh, args);
        }catch (Exception e){}

    }
}
