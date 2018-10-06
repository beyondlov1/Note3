package com.beyond.fly.note3.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by beyond on 2017/12/24.
 */

public class HttpUtils {

    public String createSuccessful="0";
    public String readSuccessful="0";
    public String deleteSuccessful="0";
    public String createTableSuccessful="0";
    public String updateSuccessful="0";
    public String isExistSuccessful="0";
    private static String keyOfDataArray ="data";
    public static String onlineTableName="beyond";

    private HttpsURLConnection httpsURLConnection;

    public void createWithHttpUrlConnection(String[] keys,Object[] values){

        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_create_item.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection) url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setReadTimeout(8000);
            httpsURLConnection.setConnectTimeout(8000);

            //编写requestBody
            byte[] requestBody=null;
            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            requestBody=stringBuilder.toString().getBytes("UTF-8");
            //编写outputStream
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            //获取response
            StringBuilder response=new StringBuilder();
            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            createSuccessful=jsonObject.getString("success");

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
    }

    public void createWithIdWithHttpUrlConnection(String[] keys,Object[] values){

        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_create_item_with_id.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection) url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setReadTimeout(8000);
            httpsURLConnection.setConnectTimeout(8000);

            //编写requestBody
            byte[] requestBody=null;
            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            requestBody=stringBuilder.toString().getBytes("UTF-8");
            //编写outputStream
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            //获取response
            StringBuilder response=new StringBuilder();
            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            createSuccessful=jsonObject.getString("success");

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
    }

    public ArrayList<MyHashmap> readWithHttpUrlConnection(String[] keys,String[] values){

        ArrayList<MyHashmap> data=new ArrayList<>();

        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_read_table.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection)url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(8000);
            httpsURLConnection.setReadTimeout(8000);

            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            byte[] requestBody=stringBuilder.toString().getBytes("UTF-8");
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            readSuccessful=jsonObject.getString("success");

            if (readSuccessful.equals("1")){
                JSONArray jsonArray=jsonObject.getJSONArray(keyOfDataArray);

                for (int i=0;i<jsonArray.length();i++){
                    MyHashmap hashMap=new MyHashmap();
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);

                    Object _id=jsonObject1.get("_id");
                    Object title=jsonObject1.get("title");
                    Object content=jsonObject1.get("content");
                    Object dates=jsonObject1.get("dates");
                    Object remind_date=jsonObject1.get("remind_date");
                    Object color=jsonObject1.get("color");
                    Object calendar_event_id=jsonObject1.get("calendar_event_id");
                    Object type=jsonObject1.get("type");
                    Object creator=jsonObject1.get("creator");

                    hashMap.put("_id",_id);
                    hashMap.put("title",title);
                    hashMap.put("content",content);
                    hashMap.put("dates",dates);
                    hashMap.put("remind_date",remind_date);
                    hashMap.put("color",color);
                    hashMap.put("calendar_event_id",calendar_event_id);
                    hashMap.put("type",type);
                    hashMap.put("creator",creator);
                    data.add(hashMap);
                }
                return data;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
        return data;
    }

    public boolean isUserExistWithHttpUrlConnection(String[] keys,String[] values){

        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_is_user_exist.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection)url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(8000);
            httpsURLConnection.setReadTimeout(8000);

            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            byte[] requestBody=stringBuilder.toString().getBytes("UTF-8");
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            isExistSuccessful=jsonObject.getString("success");

            if (isExistSuccessful.equals("1")) {
                return true;
            }else if (isExistSuccessful.equals("0")){
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
        return true;
    }

    public void deleteItemWithHttpUrlConnection(String[] keys, Object[] values){
        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_delete_item.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection)url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(8000);
            httpsURLConnection.setReadTimeout(8000);

            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            byte[] requestBody=stringBuilder.toString().getBytes("UTF-8");
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            deleteSuccessful=jsonObject.getString("success");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }

    }

    public void createTableWithHttpURLConnection(String[] keys, String[] values) {
        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_create_table.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection)url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(8000);
            httpsURLConnection.setReadTimeout(8000);

            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            byte[] requestBody=stringBuilder.toString().getBytes("UTF-8");
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            createTableSuccessful=jsonObject.getString("success");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
    }

    public void updateItemWithHttpUrlConnection(String[] keys,Object[] values){
        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_update_item.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection) url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setReadTimeout(8000);
            httpsURLConnection.setConnectTimeout(8000);

            //编写requestBody
            byte[] requestBody=null;
            StringBuilder stringBuilder=new StringBuilder();
            for (int i=0;i<keys.length;i++){
                stringBuilder.append(keys[i]);
                stringBuilder.append("=");
                stringBuilder.append(values[i]);
                if (i<keys.length-1){
                    stringBuilder.append("&");
                }
            }
            requestBody=stringBuilder.toString().getBytes("UTF-8");
            //编写outputStream)
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            //获取response
            StringBuilder response=new StringBuilder();
            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            updateSuccessful=jsonObject.getString("success");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
    }

    public ArrayList<MyHashmap> readFriendDataWithHttpUrlConnection(String key,Object[] values){

        ArrayList<MyHashmap> data=new ArrayList<>();

        httpsURLConnection=null;
        String url="https://beyondlov1.000webhostapp.com/note_3/note_read_friend_table.php";
        try {
            URL url1=new URL(url);
            httpsURLConnection=(HttpsURLConnection)url1.openConnection();
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(8000);
            httpsURLConnection.setReadTimeout(8000);

            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("tablename=user_name&&sql=SELECT * From user_name WHERE ");
            for (int i=0;i<values.length;i++){
                stringBuilder.append(key);
                stringBuilder.append("=");
                stringBuilder.append("'");
                stringBuilder.append(values[i]);
                stringBuilder.append("'");
                if (i<values.length-1) {
                    stringBuilder.append(" or ");
                }
            }
            stringBuilder.append(" ORDER BY _id DESC");
            byte[] requestBody=stringBuilder.toString().getBytes("UTF-8");
            OutputStream outputStream=httpsURLConnection.getOutputStream();
            outputStream.write(requestBody);
            outputStream.flush();
            outputStream.close();

            InputStream inputStream=httpsURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response=new StringBuilder();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                response.append(line);
            }
            JSONObject jsonObject=new JSONObject(response.toString());
            readSuccessful=jsonObject.getString("success");

            if (readSuccessful.equals("1")){
                JSONArray jsonArray=jsonObject.getJSONArray(keyOfDataArray);

                for (int i=0;i<jsonArray.length();i++){
                    MyHashmap hashMap=new MyHashmap();
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);

                    Object _id=jsonObject1.get("_id");
                    Object title=jsonObject1.get("title");
                    Object content=jsonObject1.get("content");
                    Object dates=jsonObject1.get("dates");
                    Object remind_date=jsonObject1.get("remind_date");
                    Object color=jsonObject1.get("color");
                    Object calendar_event_id=jsonObject1.get("calendar_event_id");
                    Object type=jsonObject1.get("type");
                    Object creator=jsonObject1.get("creator");

                    hashMap.put("_id",_id);
                    hashMap.put("title",title);
                    hashMap.put("content",content);
                    hashMap.put("dates",dates);
                    hashMap.put("remind_date",remind_date);
                    hashMap.put("color",color);
                    hashMap.put("calendar_event_id",calendar_event_id);
                    hashMap.put("type",type);
                    hashMap.put("creator",creator);
                    data.add(hashMap);
                }
                return data;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (httpsURLConnection!=null){
                httpsURLConnection.disconnect();
            }
        }
        return data;
    }
}
