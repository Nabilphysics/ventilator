package com.crux.ventapp.utils;

import android.content.Context;

public class AlarmController {

    private boolean vtHighAlarmStatus;
    private boolean vtLowAlarmStatus;
    private boolean pipHighAlarmStatus;
    private boolean pipLowAlarmStatus;
    private boolean peepHighAlarmStatus;
    private boolean peepLowAlarmStatus;
    private Context context;
    private CustomDialog customDialog;
    private SoundPlayer soundPlayer;
    private boolean isStarted;

    public AlarmController(Context context, SoundPlayer soundPlayer) {

        this.context = context;
        this.soundPlayer = soundPlayer;


    }

    public void playAlarm() {
        soundPlayer.playAlarm();
        setStarted(true);
    }

    public void pauseAlarm() {
        soundPlayer.pauseAlarm();
    }

    public boolean isPlaying() {
        return soundPlayer.isPlaying();
    }


    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isStarted() {
        return isStarted;
    }


    public interface OnAlarmDialogCloseListener {
        void onAlarmDialogClose();
    }
}
