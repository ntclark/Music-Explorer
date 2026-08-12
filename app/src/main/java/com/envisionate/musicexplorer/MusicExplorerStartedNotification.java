package com.envisionate.musicexplorer;

import static android.content.Intent.getIntent;
import static android.os.Looper.getMainLooper;

import static com.envisionate.musicexplorer.Globals.theMusicExplorerInterface;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;

@ExperimentalCarApi
public class MusicExplorerStartedNotification extends BroadcastReceiver {

    public MusicExplorerStartedNotification() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
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
