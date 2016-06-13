package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

            int bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mBuffer = new short[bufferSize];
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

//            mRecorder=new Mp3Recorder();
//            mRecorder=ExtAudioRecorder.getInstanse(true);
//            mRecorder.setOutputFile(file.getAbsolutePath());

//            mRecorder = new MediaRecorder();
//            // 设置输出文件
//            mRecorder.setOutputFile(file.getAbsolutePath());
//
//            // 设置meidaRecorder的音频源是麦克风
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//对有些手机会报错:java.lang.RuntimeException: setAudioSource failed.
//            // 设置文件音频的输出格式为amr
//            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
//            // 设置音频的编码格式为amr
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//            // 严格遵守google官方api给出的mediaRecorder的状态流程图
//            mRecorder.prepare();
////
//            mRecorder.start();

            isRecording = true;

            mAudioRecord.startRecording();
            startBufferedWrite(file);

            // 准备结束
            isPrepared = true;
            // 已经准备好了，可以录制了
            if (mListener != null) {
                mListener.wellPrepared();
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 开始录音
     */
    public void startRecord() {
        if (mAudioRecord != null && !isRecording) {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {

                new StartRecordThread().start();
                isRecording = true;
            } else {
//                LogUtil.e(TAG, "mAudioRecord=STATE_UNINITIALIZED");
            }
        }
    }


    class StartRecordThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            mAudioRecord.startRecording();
            writeDateToFile(mCurrentFilePathString);
        }
    }

    /**
     * 写入原数据
     *
     * @param path
     */
    private void writeDateToFile(String path) {
        short[] buffDate = new short[bufferSize];
        DataOutputStream dos = null;
        int readSize = 0;
        try {
            File recordFile = new File(path);
            if (recordFile.exists()) {
                recordFile.delete();
                recordFile.createNewFile();
            }
            dos = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(recordFile)));
            while (isRecording) {
                readSize = mAudioRecord.read(buffDate, 0, bufferSize);
                for (int i = 0; i < readSize; i++) {
                    dos.writeShort(buffDate[i]);
                }
//                LogUtil.i(TAG, "录入音频大小:" + buffDate.length);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (isRecording) {
                        int readSize = mAudioRecord.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            output.flush();
                        }
                    }
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generalFileName() {
        // TODO Auto-generated method stub

        return UUID.randomUUID().toString() + ".raw";
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
        if (mAudioRecord != null) {
            mAudioRecord.release();
//            mAudioRecord = null;
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
