package com.ldz.fileman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ListView listV;

    ArrayList<String> files = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listV = findViewById(R.id.list);

        try{
            getFile("/");
        }catch (Exception e){}
        if(files.size() == 0){
            try{
                getFile("/sdcard");
            }catch (Exception e){}
        }

        try {

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, files);
            // присваиваем адаптер списку
            listV.setAdapter(adapter);

            adapter.notifyDataSetChanged();

            listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getFile(files.toArray()[position].toString());
                    adapter.clear();
                    adapter.addAll(files);

                }
            });
        }catch (Exception e){e.printStackTrace();}

    }

    public void getFile(String path){
            // Read all files sorted into the values-array
        ArrayList<String> ff= new ArrayList<String>();
        File dir = new File(path);
        if (!dir.canRead()) {
            Log.e("cannot","inaccessible");
        }

        File[] list = dir.listFiles();
        try {
            if (list != null) {
                ff.add("/sdcard");
                for (File file : list) {
                    if (!file.getName().startsWith(".")) {
                        ff.add(file.getAbsolutePath());
                    }
                }
                files =ff;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
