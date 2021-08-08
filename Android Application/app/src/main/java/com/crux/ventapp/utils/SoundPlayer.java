package com.crux.ventapp.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

public class SoundPlayer {

    private Context context;
    MediaPlayer mediaPlayer;

    public SoundPlayer(Context context) {
        this.context = context;
    }

    public void setAlarmSoundClip(int soundClipId) {
        mediaPlayer = MediaPlayer.create(context, soundClipId);
    }

    public void playAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0.2f, 0.2f);
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        } else {
            Toast.makeText(context, "Media Player is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void enableLooping() {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        } else {
            Toast.makeText(context, "Media Player is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void disableLooping() {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(false);
        } else {
            Toast.makeText(context, "Media Player is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        } else {
            Toast.makeText(context, "Media Player is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        } else {
            Toast.makeText(context, "Media Player is null", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }


}
