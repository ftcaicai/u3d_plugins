package com.huihe.gameapp;


import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.AudioRecord;
import android.util.Log;

public class AudioRecorder {
    private AudioRecord recorder;
    private int sampleRate;
    private String wavFilePath;
    private byte[] buffer;
    private int bufferSize;
    private boolean recording;
    private RandomAccessFile wavFile; // 保存wav数据的文件对象
    private int totalSize;      // wav数据总长度
    private int bSamples = 16;  // 每个采样点位数，与 AudioFormat.ENCODING_PCM_16BIT 对应
    private int nChannels = 1;  // 通道数，与 AudioFormat.CHANNEL_IN_MONO 对应

    private AudioManager audioManager;
    private int maxVolumeLevel;

    private static AudioRecorder instance;

    class AudioRecordThread implements Runnable{
        @Override
        public void run(){
            try{
                // 准备用来写入数据的文件
                wavFile = new RandomAccessFile(wavFilePath, "rw");
                wavFile.setLength(0); // 清空文件内容
                byte[] header = new byte[44];
                wavFile.write(header); // 写入44空字节的头部(占位)

                while(recording){
                    int result = recorder.read(buffer, 0, buffer.length);
                    if(result == AudioRecord.ERROR_INVALID_OPERATION){
                        // 忽略，不用提示
                        break;
                    }else if(result == AudioRecord.ERROR_BAD_VALUE){
                        Log.e("Unity", "AudioRecord.read()读取录音数据时发生错误: Bad value error");
                        break;
                    }else if(result == AudioRecord.ERROR) {
                        Log.e("Unity", "AudioRecord.read()读取录音数据时发生错误: Unknown error");
                        break;
                    }

                    wavFile.write(buffer);
                    totalSize += buffer.length;
                }

                // 写入文件头
                writeWavHeader(wavFile, totalSize, nChannels, sampleRate, bSamples);

                wavFile.close();
//                Log.v("Unity", "录音数据写入完成");
            }catch(IOException e){
                Log.e("Unity", "写入录音数据时发生异常: " + e.getMessage());
            }
        }
    }

    public static AudioRecorder getInstance(){
        if(null == instance){
            instance = new AudioRecorder();
        }
        return instance;
    }

    public void setCurrentActivity(Activity activity){
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        maxVolumeLevel = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
    }

    /**
     * 获取当前音量(原始值)
     * @return 音量等级
     */
    public int getVolumeRaw(){
        return audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
    }

    /**
     * 设置音量(原始值)
     * @param vol 音量等级
     */
    public void setVolumeRaw(int vol){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
    }

    /**
     * 获取当前音量(百分比)
     * @return 返回0~100之间的值表示百分比
     */
    public int getVolume(){
        int current = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        int vol = (int)((float)current / maxVolumeLevel * 100);
//        Log.v("Unity", "获取音量,current:" + current + " max:" + maxVolumeLevel + " vol:" + vol);
        return vol;
    }

    /**
     * 设置音量(百分比)
     * @param percent 百分比,最小0,最大100
     */
    public void setVolume(int percent){
        if(percent < 0 || percent > 100) return;

        int vol = (maxVolumeLevel * percent / 100);
//        Log.v("Unity", "设置音量: " + vol);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
    }

    /**
     * 初始化
     * @param sampleRate 采样率
     * @param filepath 临时语音文件保存地址
     * @return 是否成功
     */
    public boolean init(int sampleRate, String filepath){
        if(null != recorder){
            Log.e("Unity", "AudioRecord不能重复初始化");
            return false;
        }

        wavFilePath = filepath;
        this.sampleRate = sampleRate;

        int source = MediaRecorder.AudioSource.MIC;
        int channel = AudioFormat.CHANNEL_IN_MONO;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding);
        recorder = new AudioRecord(source, sampleRate, channel, encoding, bufferSize);
        if(recorder.getState() != AudioRecord.STATE_INITIALIZED){
            Log.e("Unity", "AudioRecord初始化失败: " + recorder.getState());
            return false;
        }

        return true;
    }

    /**
     * 开始录音
     * @return 是否成功
     */
    public boolean start(){
        if(null == recorder) return false;
        if(recording) return false;

        totalSize = 0;
        buffer = new byte[bufferSize];

        recorder.startRecording();
        recording = true;

        // 启动录音线程
        new Thread(new AudioRecordThread()).start();

        return true;
    }

    /**
     * 停止录音
     */
    public void stop(){
        if(null == recorder) return;
        if(!recording) return;

        recording = false;
        recorder.stop();

//        Log.v("Unity", "=> 停止录音");
    }

    /**
     * 是否正在录音中
     * @return boolean
     */
    public boolean isRecording(){
        return recording;
    }

    // 写入wav文件头
    private void writeWavHeader(RandomAccessFile file, int dataLen, int channels, int sampleRate, int byteRate) throws IOException {
        int totalLen = dataLen + 36;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalLen & 0xff);
        header[5] = (byte) ((totalLen >> 8) & 0xff);
        header[6] = (byte) ((totalLen >> 16) & 0xff);
        header[7] = (byte) ((totalLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (dataLen & 0xff);
        header[41] = (byte) ((dataLen >> 8) & 0xff);
        header[42] = (byte) ((dataLen >> 16) & 0xff);
        header[43] = (byte) ((dataLen >> 24) & 0xff);

        // 从文件头部开始写入
        file.seek(0);
        file.write(header);
    }
}