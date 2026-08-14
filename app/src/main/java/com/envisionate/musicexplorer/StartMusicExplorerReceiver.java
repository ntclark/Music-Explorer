package com.envisionate.musicexplorer;

import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.car.app.Screen;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;

public class StartMusicExplorerReceiver extends BroadcastReceiver {

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
            String strExtra = intent.getStringExtra("IsPlaying");
            if ( null == CarEntitiesScreen.getWaitingScreen() )
                return;
            if ( ! ( null == strExtra ) && "true".equals(strExtra) ) {
                playInCar(CarEntitiesScreen.getWaitingScreen());
                return;
            }
            CarEntitiesScreen.getWaitingScreen().clearTemplate();
            CarEntitiesScreen.getWaitingScreen().invalidate();
            return;
        }

        if ( "com.envisionate.musicexplorer.PLAY_NOTIFY".equals(action) ) {
            if ( null == Globals.currentScreen )
                return;
            playInCar(Globals.currentScreen);
            return;
        }

        if ( "com.envisionate.musicexplorer.NAVIGATION_NOTIFY".equals(action) ) {
            if ( null == Globals.currentScreen )
                return;
            ((CarEntitiesScreen)Globals.currentScreen).clearTemplate();
            ((CarEntitiesScreen)Globals.currentScreen).invalidate();
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

