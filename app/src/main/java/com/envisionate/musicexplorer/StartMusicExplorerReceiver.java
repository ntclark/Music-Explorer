package com.envisionate.musicexplorer;

import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.OptIn;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;

public class StartMusicExplorerReceiver extends BroadcastReceiver {

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ( "com.envisionate.musicexplorer.START".equals(action) ) {
            Intent startOnPhone = new Intent(context, MusicExplorer.class);
            startOnPhone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startOnPhone.putExtra("StartedByCar","true");
            context.startActivity(startOnPhone);
        }
        if ( "com.envisionate.musicexplorer.STARTED".equals(action) ) {
            String strExtra = intent.getStringExtra("IsPlaying");
            if ( ! ( null == strExtra ) && strExtra.equals("true") ) {
                CarPlayerScreen carPlayerScreen = new CarPlayerScreen(CarEntitiesScreen.getWaitingScreen().getCarContext());
                theMusicPlayer.addListener(carPlayerScreen);
                CarEntitiesScreen.getWaitingScreen().getScreenManager().push(carPlayerScreen);
                CarEntitiesScreen.getWaitingScreen().clearTemplate();
                MediaMetadata md = new MediaMetadata.Builder()
                        .setAlbumTitle(properties.getCurrentFolder().getName())
                        .setTitle(properties.getCurrentFile().getName())
                        .build();
                carPlayerScreen.onMediaMetadataChanged(md);
                return;
            }
            CarEntitiesScreen.getWaitingScreen().clearTemplate();
            CarEntitiesScreen.getWaitingScreen().invalidate();
        }
    }

}

