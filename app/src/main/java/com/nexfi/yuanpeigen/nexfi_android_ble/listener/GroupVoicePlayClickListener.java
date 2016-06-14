package com.nexfi.yuanpeigen.nexfi_android_ble.listener;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.GroupChatAdapater;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;

import java.io.File;

/**
 * Created by gengbaolong on 2016/6/5.
 */
public class GroupVoicePlayClickListener implements View.OnClickListener {
    public static GroupVoicePlayClickListener currentPlayListener = null;
    GroupChatMessage groupChatMessage;
    ImageView voiceIconView;
    GroupChatAdapater groupChatAdapater;
    String userSelfId;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    public static boolean isPlaying = false;
    int position;

    public GroupVoicePlayClickListener(GroupChatMessage groupChatMessage, ImageView imageView, String userSelfId, GroupChatAdapater groupChatAdapater,int position) {
        this.groupChatMessage=groupChatMessage;
        this.voiceIconView = imageView;
        this.groupChatAdapater = groupChatAdapater;
        this.userSelfId = userSelfId;
        this.position=position;
    }


    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (
            groupChatMessage.userMessage.userId.equals(userSelfId)) {
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
        groupChatAdapater.notifyDataSetChanged();
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
        if (groupChatMessage.userMessage.userId.equals(userSelfId)) {
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
        playVoice(groupChatMessage.voiceMessage.filePath);
    }
}
