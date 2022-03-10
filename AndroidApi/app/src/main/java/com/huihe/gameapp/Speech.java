package com.huihe.gameapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.speech.util.JsonParser;
import com.unity3d.player.UnityPlayer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class Speech {
    Context mContext = null;

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 最后一次的完整识别结果
    protected String fullText = "";

    private static Speech instance;

    public static Speech getInstance(){
        if(null == instance){
            instance = new Speech();
        }
        return instance;
    }

    public Speech(){
    }

    public void init(Activity activity) {
        Log.v("Unity", "正在启动语音识别服务");
        mContext = activity.getApplicationContext();
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=1183946a");
        mIat = SpeechRecognizer.createRecognizer(mContext, null);
    }

    /**
     * 识别语音数据，识别结果会通过UnitySendMessage直接发送回Unity中
     * @param  audioData 语音数据
     */
    public void speechRecognize(byte[] audioData){
        // 清空上次的结果
        mIatResults.clear();
        fullText = "";

        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.SAMPLE_RATE, "16000");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置音频来源为字节数组
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            sendMessage(1, "识别失败,错误码：" + ret);
        } else {
            if (null != audioData) {
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别
                mIat.writeAudio(audioData, 0, audioData.length);
                mIat.stopListening();
            } else {
                mIat.cancel();
                sendMessage(1, "读取音频流失败");
            }
        }
    }

    // Iat发送给unity的消息
    private void sendMessage(int errCode, String msg){
        String result = "{\"errCode\": " + errCode + ", \"message\": \"" + msg + "\"}";
        UnityPlayer.UnitySendMessage("SpeechProxy", "MessageArrive", result);
    }

    // 解析结果，返回文本
    private String resultToText(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
    }

    // 听写监听器。
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        }

        @Override
        public void onError(SpeechError error) {
            // 提示：错误码: 10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            sendMessage(error.getErrorCode(), error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            // Log.v("Unity", "识别结果: " + results.getResultString());
            fullText += resultToText(results);
            if (isLast) {
                sendMessage(0, resultToText(results));
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            // sendMessage(2, "音量太小：" + volume );
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
}
