package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.czt.mp3recorder.MP3Recorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioManager {

    private MediaRecorder mRecorder;
    private String mDirString;
    private String mCurrentFilePathString;

    AudioRecord mAudioRecord;
    private boolean isPrepared;// 是否准备好了
    boolean isRecording = false;


    private final int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050,16000,11025
    private final int sampleRateInHz = 16000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 单例化的方法 1 先声明一个static 类型的变量a 2 在声明默认的构造函数 3 再用public synchronized static
     * 类名 getInstance() { if(a==null) { a=new 类();} return a; } 或者用以下的方法
     */

    /**
     * 单例化这个类
     */
    private static AudioManager mInstance;
    private short[] mBuffer;
    private int bufferSize;
    private MP3Recorder mp3Recorder;

    private AudioManager(String dir) {
        mDirString = dir;
    }

    public static AudioManager getInstance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);

                }
            }
        }
        return mInstance;

    }

    /**
     * 回调函数，准备完毕，准备好后，button才会开始显示录音框
     *
     * @author nickming
     */
    public interface AudioStageListener {
        void wellPrepared();
    }

    public AudioStageListener mListener;

    public void setOnAudioStageListener(AudioStageListener listener) {
        mListener = listener;
    }

    // 准备方法
    public void prepareAudio() {
        try {
            // 一开始应该是false的
            isPrepared = false;

            File dir = new File(mDirString);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileNameString = generalFileName();
            File file = new File(dir, fileNameString);

            mCurrentFilePathString = file.getAbsolutePath();

            mp3Recorder = new MP3Recorder(file);

            mp3Recorder.start();

            // 准备结束
            isPrepared = true;
            // 已经准备好了，可以录制了
            if (mListener != null) {
                mListener.wellPrepared();
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generalFileName() {
        // TODO Auto-generated method stub

        return UUID.randomUUID().toString() + ".mp3";
    }

    // 获得声音的level
    public int getVoiceLevel(int maxLevel) {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
//        if (isPrepared) {
//            try {
//                // 取证+1，否则去不到7
//                return maxLevel * mRecorder.getMaxAmplitude() / 32768 + 1;
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//
//            }
//        }

        return 1;
    }

    // 释放资源
    public void release() {
        // 严格按照api流程进行
        if (mp3Recorder != null) {
            mp3Recorder.stop();
        }

    }

    // 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
    // 这是与release的方法的区别
    public void cancel() {
        release();
        if (mCurrentFilePathString != null) {
            File file = new File(mCurrentFilePathString);
            file.delete();
            mCurrentFilePathString = null;
        }

    }

    public String getCurrentFilePath() {
        // TODO Auto-generated method stub
        return mCurrentFilePathString;
    }


}
