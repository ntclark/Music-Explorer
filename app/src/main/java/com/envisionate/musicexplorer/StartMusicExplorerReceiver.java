package com.envisionate.musicexplorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.OptIn;
import androidx.car.app.annotations.ExperimentalCarApi;

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
    }

}

