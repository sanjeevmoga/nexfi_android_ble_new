package com.nexfi.yuanpeigen.nexfi_android_ble.listener;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.ChatMessageAdapater;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.SingleChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;

import java.io.File;

/**
 * Created by gengbaolong on 2016/6/5.
 */
public class VoicePlayClickListener implements View.OnClickListener {
    public static VoicePlayClickListener currentPlayListener = null;
    SingleChatMessage singleChatMessage;
    ImageView voiceIconView;
    ChatMessageAdapater chatMessageAdapater;
    String userSelfId;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    public static boolean isPlaying = false;


    public VoicePlayClickListener(SingleChatMessage singleChatMessage, ImageView imageView, String userSelfId, ChatMessageAdapater chatMessageAdapater) {
        this.singleChatMessage = singleChatMessage;
        this.voiceIconView = imageView;
        this.chatMessageAdapater = chatMessageAdapater;
        this.userSelfId = userSelfId;
    }


    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (singleChatMessage
                .userMessage.userId.equals(userSelfId)) {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        chatMessageAdapater.notifyDataSetChanged();
    }


    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();
        } catch (Exception e) {
        }
    }


    private void showAnimation() {
        // play voice, and start animation
        if (singleChatMessage.userMessage.userId.equals(userSelfId)) {
            voiceIconView.setImageResource(R.drawable.voice_to_icon);
        } else {
            voiceIconView.setImageResource(R.drawable.voice_from_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }


    @Override
    public void onClick(View v) {
        if (isPlaying) {
            currentPlayListener.stopPlayVoice();
        }
        playVoice(singleChatMessage.voiceMessage.filePath);
    }
}
