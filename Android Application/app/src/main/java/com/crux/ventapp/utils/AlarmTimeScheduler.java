package com.crux.ventapp.utils;

import android.os.Handler;

public class AlarmTimeScheduler {

    private Handler handler;
    private Runnable myRunnable;
    private long scheduleTime;

    public AlarmTimeScheduler(){
       handler = new Handler();
    }

    public void setScheduleTime(long millis){
        this.scheduleTime = millis;
    }

    public void startSchedule(){
        myRunnable = new Runnable() {
            @Override
            public void run() {

            }
        };

    }
}
