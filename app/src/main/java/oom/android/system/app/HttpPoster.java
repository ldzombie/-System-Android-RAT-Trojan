package oom.android.system.app;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HttpPoster implements Runnable {
    private String file_to_upload;//файл для загрузки на сервер
    private String data;//передаваемая информация
    private List<String> listdata = new ArrayList<String>();//
    private boolean isfile=false;//является ли передаваемое файлом
    private boolean isarray=false;//является ли передаваемое массивом
    String post_url;//Ссылка для отправки data по ссылке


    public HttpPoster(String file) {
        isfile=true;
        file_to_upload=file;
    }

    public HttpPoster(String url, String data){
        isfile=false;
        this.data=data;
        post_url=url;
    }

    public HttpPoster(String url, List<String> listdata){
        isarray=true;
        this.listdata=listdata;
        post_url=url;
    }


    @Override
    public void run() {
        if(isfile){
            postfile();
        }
        else if(isarray){
            postarray();
        }
        else{
            post();
        }
    }
    //отправка файла
    public void postfile(){
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        String pathToOurFile = file_to_upload;
        String uploader_url = MyService.uploader_url;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        int bufferSize = 1024;
        int bytesRead;

        try
        {
            byte[] buffer = new byte[bufferSize];

            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
            URL url = new URL(uploader_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(bufferSize);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
            //application/octet-stream   multipart/form-data
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""+pathToOurFile +"\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                if(Thread.currentThread().isInterrupted()){
                    fileInputStream.close();
                    outputStream.close();
                    throw new InterruptedException();}
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            int serverResponseCode = connection.getResponseCode();
            if (serverResponseCode==200){
                Log.d(MyService.LOG_TAG, "файл "+file_to_upload+" успешно загружен!");
            }
            else{
                Log.d(MyService.LOG_TAG, "файл  "+file_to_upload+" был загружен неправильно");
                String serverResponseMessage = connection.getResponseMessage();
                Log.d(MyService.LOG_TAG, "ответ кода: "+serverResponseCode);
                Log.d(MyService.LOG_TAG, "сообщение:\n"+serverResponseMessage);
            }

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e)
        {
            Log.d(MyService.LOG_TAG, "error uploading file "+file_to_upload+"\n"+e.toString());
        }

    }
    //отпрака текста
    public void post(){
        try{
            URL obj = new URL(post_url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                //Log.d(MyService.LOG_TAG,"P Ответ кода: HTTP_OK");
            } else {
                Log.d(MyService.LOG_TAG,"POST запрос не сработал\nОтвет кода:"+responseCode);
            }
        }
        catch(Exception e){
            Log.d(MyService.LOG_TAG,"ошибка отправки POST\n"+e.toString());
        }

    }
    //отпрака массива
    public void postarray(){
        try{
            URL obj = new URL(post_url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            for (int i=0; i<listdata.size();i++){
                os.write(listdata.get(i).getBytes());
                os.write((byte) '\n');
            }
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                //Log.d(MyService.LOG_TAG,"PA Ответ кода: HTTP_OK");
            } else {
                Log.d(MyService.LOG_TAG,"POST запрос не сработал\nОтвет кода:"+responseCode);
            }
        }
        catch(Exception e){
            Log.d(MyService.LOG_TAG,"ошибка отправки POST\n"+e.toString());
        }

    }


}
