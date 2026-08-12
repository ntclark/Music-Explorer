package com.envisionate.musicexplorer;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ) {

        for ( long k = 0; k < 100; k++ )
        Log.d("MusicExplorer","BLuetooth has connected");

            Intent activityIntent = new Intent(context, MusicExplorer.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
