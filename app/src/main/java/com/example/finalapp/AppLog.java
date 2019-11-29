package com.example.finalapp;

import android.util.Log;


public class AppLog {

    final int REQUEST_PERMISSION_CODE=1000;

    private static final String APP_TAG = "AudioRecorder";

    public static int logString(String message){
        return Log.i(APP_TAG,message);
    }
}
