package com.envisionate.musicexplorer;

import static android.os.Looper.getMainLooper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.car.app.annotations.ExperimentalCarApi;

@ExperimentalCarApi
public class StartMusicExplorer extends Thread {

    private Context theContext;
    private static String action = "com.envisionate.musicexplorer.START";

    public static class SetupMusicExplorer extends StartMusicExplorer {
        public SetupMusicExplorer(Context ct) {
            super(ct);
            action = "com.envisionate.musicexplorer.SETUP_NOTIFY";
        }
    }

    public StartMusicExplorer(Context ct) {
        theContext = ct;
    }

    public void run() {
/*    try {
        Thread.sleep(10000);
    } catch ( Exception ex ) { }*/
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(action);
                intent.setPackage("com.envisionate.musicexplorer");
                theContext.getApplicationContext().sendBroadcast(intent);
            }
        });
    }

}
