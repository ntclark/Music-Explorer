package com.envisionate.musicexplorer;

import static android.os.Looper.getMainLooper;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.core.content.ContextCompat;

import com.envisionate.carservice.screen.CarEntitiesScreen;

@ExperimentalCarApi
public class StartMusicExplorer extends Thread {

    public static MusicExplorerStartedNotification theNotification = null;

    private Context theContext;
    private CarEntitiesScreen theWaitingScreen = null;

    public StartMusicExplorer(CarEntitiesScreen ceScreen) {
        theWaitingScreen = ceScreen;
        theContext = ceScreen.getCarContext();
    }

    public void run() {
/*    try {
        Thread.sleep(7000);
    } catch ( Exception ex ) { }*/

        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("com.envisionate.musicexplorer.START");
                intent.setPackage("com.envisionate.musicexplorer");
                theContext.getApplicationContext().sendBroadcast(intent);
            }
        });
    }

}
