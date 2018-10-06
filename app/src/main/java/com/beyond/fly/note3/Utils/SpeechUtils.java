package com.beyond.fly.note3.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.fly.note3.MainActivity;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by beyond on 2018/1/20.
 */

public class SpeechUtils {

    private Context context;
    private static String TAG="Tag";
    private com.iflytek.cloud.SpeechRecognizer speechRecognizer;
    private RecognizerDialog recognizerDialog;
    private InitListener initListener;
    private static String APP_ID="5a619106";
    public EditText resultEditText;
    public TextView textView;
    private HashMap<String,String> result=new LinkedHashMap<>();
    private String mEngineType= SpeechConstant.TYPE_CLOUD;
    private String lastResult;
    private MyDatabaseUtils myDatabaseUtils;

    public SpeechUtils(Context context){
        this.context=context;
        SpeechUtility.createUtility(context,"appid="+APP_ID);
    }
    public void initViews(EditText resultEditText,TextView textView){
        this.resultEditText=resultEditText;
        this.textView=textView;
    }
    public void initSpeechRecognizer(){
        initListener=new InitListener() {
            @Override
            public void onInit(int i) {
                System.out.println("initListener");
            }
        };
        speechRecognizer= SpeechRecognizer.createRecognizer(context,initListener);
        recognizerDialog=new RecognizerDialog(context,initListener);
        setParams();
    }

    public void getResultText(){
        lastResult=resultEditText.getText().toString();
        result.clear();
        recognizerDialog.setListener(recognizerDialogListener);
        recognizerDialog.show();
    }

    private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            printResult(recognizerResult);
            if (b){
                myDatabaseUtils=MyDatabaseUtils.getMyDatabaseUtilsInstance(context);
                long currentDate=System.currentTimeMillis();
                String title=resultEditText.getText().toString();
                String content="";
                String[] keys = new String[] {"title","content","dates","type","creator"};
                Object[] values = new Object[] {title,content,currentDate,"remind",MyDatabaseUtils.tableName};
                myDatabaseUtils.insertDataToDatabase(MyDatabaseUtils.tableName,keys,values);
                Intent intent=new Intent();
                intent.setAction(MainActivity.REFRESH);
                context.sendBroadcast(intent);
                resultEditText.setText(null);
                resultEditText.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                Toast.makeText(context,"ok",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };
    private void printResult(RecognizerResult recognizerResult){

        String text=parseIatResult(recognizerResult.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        result.put(sn, text);
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : result.keySet()) {
            resultBuffer.append(result.get(key));
        }

        String finalResult=lastResult+resultBuffer.toString();
        resultEditText.setText(finalResult);
        resultEditText.setSelection(resultEditText.length());
    }
    private String parseIatResult(String json) {
        StringBuilder ret = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
//				如果需要多候选结果，解析数组其他字段
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    private void setParams(){
        if (speechRecognizer==null){
            System.out.println("abc");
        }
        // 清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS, "2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }
}
