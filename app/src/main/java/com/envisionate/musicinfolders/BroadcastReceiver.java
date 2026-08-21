package com.envisionate.musicinfolders;

import static com.envisionate.musicinfolders.Globals.ANDROID_AUTO_DELAY;
import static com.envisionate.musicinfolders.Globals.currentAutoEntitiesScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoPlayerScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoSession;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.car.app.ScreenManager;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;
import com.envisionate.musicinfolders.interfaces.IMusicExplorerPlay;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d("MusicExplorer",String.format("OnReceive called with %s",action));

        if ( "com.envisionate.musicinfolders.START".equals(action) ) {
            Intent startOnPhone = new Intent(context, MusicExplorer.class);
            startOnPhone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startOnPhone.putExtra("StartedByCar","true");
            context.startActivity(startOnPhone);
            return;
        }

        if ( "com.envisionate.musicinfolders.STARTED".equals(action) ||
                "com.envisionate.musicinfolders.SETUP_DONE".equals(action) ) {
            if ( null == CarEntitiesScreen.getWaitingScreen() )
                return;
            Util.doLater(new Runnable() {
                @Override
                public void run() {
                    CarEntitiesScreen.getWaitingScreen().clearTemplate();
                    CarEntitiesScreen.getWaitingScreen().invalidate();
                    CarEntitiesScreen.clearWaitingScreen();
                }
            },ANDROID_AUTO_DELAY);
            return;
        }

        if ( "com.envisionate.musicinfolders.STARTED_WITH_PLAY".equals(action) ) {
            if ( null == CarEntitiesScreen.getWaitingScreen() )
                return;
            Util.doLater(new Runnable() {
                @Override
                public void run() {
                    playInCar();
                    CarEntitiesScreen.clearWaitingScreen();
                }
            },ANDROID_AUTO_DELAY);
            return;
        }

        if ( "com.envisionate.musicinfolders.PLAY_NOTIFY".equals(action) ) {
            if ( null == currentAutoScreen )
                return;
            Util.doLater(new Runnable() {
                @Override
                public void run() {
                    playInCar();
                }
            },ANDROID_AUTO_DELAY);
            return;
        }

        if ( "com.envisionate.musicinfolders.PLAY_NOTIFY_UPDATE_TRACK_INFO".equals(action) ) {
            if ( null == currentAutoPlayerScreen )
                return;
            Util.doLater(new Runnable() {
                @Override
                public void run() {
                    currentAutoPlayerScreen.onMediaMetadataChanged(theMusicPlayer.getPlayer().getMediaMetadata());
                }
            },ANDROID_AUTO_DELAY);
            return;
        }

        if ( "com.envisionate.musicinfolders.NAVIGATION_NOTIFY".equals(action) ) {
            if ( null == currentAutoScreen)
                return;
            Runnable notifyCar = new Runnable() {
                @Override
                public void run() {
                    if ( ! ( null == currentAutoEntitiesScreen ) ) {
                        currentAutoEntitiesScreen.clearTemplate();
                        currentAutoEntitiesScreen.invalidate();
                    } else {
                        currentAutoScreen = new CarEntitiesScreen(currentAutoScreen.getScreenManager().getTop().getCarContext());
                        currentAutoScreen.getScreenManager().push(currentAutoScreen);
                    }
                }
            };
            Util.doLater(notifyCar,ANDROID_AUTO_DELAY);
            return;
        }

        if ( "com.envisionate.musicinfolders.STOP_REQUESTED".equals(action) ||
                    "com.envisionate.musicinfolders.STOP_REQUESTED_BY_CAR".equals(action) ) {
            if ( ! ( null == currentAutoSession ) ) {
                ScreenManager screenManager = currentAutoScreen.getScreenManager();
                while ( 1 < screenManager.getStackSize() )
                    screenManager.pop();
                screenManager.getTop().finish();
                currentAutoScreen.getCarContext().finishCarApp();
            }
            if ( "com.envisionate.musicinfolders.STOP_REQUESTED_BY_CAR".equals(action) && theMusicExplorer.getWasStartedByAuto() )
                Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.CAR_PLAY_STOPPED");
            else if ( "com.envisionate.musicinfolders.STOP_REQUESTED".equals(action) )
                Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.CAR_PLAY_STOPPED");
            return;
        }

        if ( "com.envisionate.musicinfolders.CAR_PLAY_STOPPED".equals(action) ) {
            if ( ! ( null == theMusicPlayer ) )
                theMusicPlayer.stop();
            theMusicExplorer.finishAndRemoveTask();
            theMusicExplorer = null;
            return;
        }

    }

    private void playInCar() {
        currentAutoPlayerScreen = new CarPlayerScreen(currentAutoSession.getCarContext());
        theMusicPlayer.addListener((Player.Listener)currentAutoPlayerScreen);
        theMusicPlayer.addListener((IMusicExplorerPlay)currentAutoPlayerScreen);
        currentAutoScreen.getScreenManager().push(currentAutoPlayerScreen);
        currentAutoPlayerScreen.onMediaMetadataChanged(theMusicPlayer.getPlayer().getMediaMetadata());
    }

}

