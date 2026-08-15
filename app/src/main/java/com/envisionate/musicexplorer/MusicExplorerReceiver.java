package com.envisionate.musicexplorer;

import static android.os.Looper.getMainLooper;
import static com.envisionate.musicexplorer.Globals.currentAutoScreen;
import static com.envisionate.musicexplorer.Globals.currentAutoSession;
import static com.envisionate.musicexplorer.Globals.properties;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.car.app.Screen;
import androidx.car.app.ScreenManager;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;

public class MusicExplorerReceiver extends BroadcastReceiver {

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("MusicExplorer",String.format("OnReceive called with %s",action));

        if ( "com.envisionate.musicexplorer.START".equals(action) ) {
            Intent startOnPhone = new Intent(context, MusicExplorer.class);
            startOnPhone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startOnPhone.putExtra("StartedByCar","true");
            context.startActivity(startOnPhone);
            return;
        }

        if ( "com.envisionate.musicexplorer.STARTED".equals(action) ||
                "com.envisionate.musicexplorer.SETUP_DONE".equals(action) ) {
            if ( null == CarEntitiesScreen.getWaitingScreen() )
                return;
            CarEntitiesScreen.getWaitingScreen().clearTemplate();
            CarEntitiesScreen.getWaitingScreen().invalidate();
            return;
        }

        if ( "com.envisionate.musicexplorer.STARTED_WITH_PLAY".equals(action) ) {
            if ( null == CarEntitiesScreen.getWaitingScreen() )
                return;
            playInCar(CarEntitiesScreen.getWaitingScreen());
            return;
        }

        if ( "com.envisionate.musicexplorer.PLAY_NOTIFY".equals(action) ) {
            if ( null == currentAutoScreen)
                return;
            playInCar(currentAutoScreen);
            return;
        }

        if ( "com.envisionate.musicexplorer.NAVIGATION_NOTIFY".equals(action) ) {
            if ( null == currentAutoScreen)
                return;
            ((CarEntitiesScreen)currentAutoScreen).clearTemplate();
            currentAutoScreen.invalidate();
            return;
        }

        if ( "com.envisionate.musicexplorer.STOP_REQUESTED".equals(action) ) {
            if ( ! ( null == currentAutoSession ) ) {
                ScreenManager screenManager = currentAutoScreen.getScreenManager();
                while ( 1 < screenManager.getStackSize() )
                    screenManager.pop();
                screenManager.getTop().finish();
                currentAutoScreen.getCarContext().finishCarApp();
            }
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Util.broadcast(theMusicExplorer,"com.envisionate.musicexplorer.CAR_PLAY_STOPPED");
                }
            });
            return;
        }

        if ( "com.envisionate.musicexplorer.CAR_PLAY_STOPPED".equals(action) ) {
            if ( ! ( null == theMusicPlayer ) )
                theMusicPlayer.stop();
            theMusicExplorer.finishAndRemoveTask();
            return;
        }

    }

    @OptIn(markerClass = ExperimentalCarApi.class)
    private void playInCar(Screen theScreen) {
        CarPlayerScreen carPlayerScreen = new CarPlayerScreen(theScreen.getCarContext());
        theMusicPlayer.addListener(carPlayerScreen);
        theScreen.getScreenManager().push(carPlayerScreen);
        MediaMetadata md = new MediaMetadata.Builder()
                .setAlbumTitle(properties.getCurrentFolder().getName())
                .setTitle(properties.getCurrentFile().getName())
                .build();
        carPlayerScreen.onMediaMetadataChanged(md);
    }

}

