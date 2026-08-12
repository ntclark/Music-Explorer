package com.envisionate.musicexplorer;

import static androidx.media3.common.Player.EVENT_TRACKS_CHANGED;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicExplorerInterface;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.envisionate.carservice.screen.CarEntitiesScreen;

public class Play extends AppCompatActivity implements Player.Listener {

    private ExoPlayer player = null;
    private PlayerView playerView = null;

    private DocumentFile thePlayingItem = null;

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if ( ! ( null == theMusicPlayer ) )
            theMusicPlayer.finish();

        theMusicPlayer = this;

        setContentView(R.layout.player_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        player = new ExoPlayer.Builder(this).build();

        playerView = findViewById(R.id.player_view);
        playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FIT);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(86400000);
        playerView.setPlayer(player);

        thePlayingItem = properties.getCurrentFile();
        if ( ! ( null == theMusicExplorerInterface.getCurrentPlayerListener() ) )
            player.addListener(theMusicExplorerInterface.getCurrentPlayerListener());

        ((TextView)findViewById(R.id.player_view_folder)).setText(properties.getCurrentFolder().getName());

        player.addListener(this);

        DocumentFile trackFolder  = null;

        if ( thePlayingItem.isDirectory() ) {
            trackFolder = thePlayingItem;
            DocumentFile[] theFiles = thePlayingItem.listFiles();
            for ( DocumentFile df : theFiles ) {
                if ( df.isDirectory() )
                    continue;
                player.addMediaItem(MediaItem.fromUri(df.getUri()));
            }
        } else {
            trackFolder = properties.getCurrentFolder();
            int myIndex = -1;
            int k = -1;
            for ( DocumentFile df : properties.getCurrentFolder().listFiles() ) {
                if ( df.isDirectory() )
                    continue;
                k++;
                player.addMediaItem(MediaItem.fromUri(df.getUri()));
                if ( df.getName().equals(thePlayingItem.getName()) )
                    myIndex = k;
            }
            player.seekTo(myIndex,0);
        }

        ((TextView)findViewById(R.id.player_view_folder)).setText(String.format("Folder: %s",trackFolder.getName()));

        androidx.media3.common.AudioAttributes theAttributes = androidx.media3.common.AudioAttributes.fromPlatformAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build());
        player.setAudioAttributes(theAttributes,true);

        player.prepare();
        player.play();

        String startedByCar = theMusicExplorer.getIntent().getStringExtra("StartedByCar");

        if ( ( ! ( null == startedByCar ) && "true".equals(startedByCar) ) || ! ( null == CarEntitiesScreen.getWaitingScreen() ) ) {
            Intent intent = new Intent("com.envisionate.musicexplorer.STARTED");
            intent.setPackage("com.envisionate.musicexplorer");
            intent.putExtra("IsPlaying","true");
            getApplicationContext().sendBroadcast(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == android.R.id.home ) {
            theMusicExplorerInterface.gotoParent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void pause() {
        player.pause();
    }

    public void previous() {
        player.seekToPrevious();
    }

    public void play() {
        player.play();
    }

    public void next() {
        player.seekToNext();
    }

    public void stop() {
        player.stop();
        finish();
    }

    public void addListener(Player.Listener obj) {
        player.addListener(obj);
    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata md) {
        ((TextView)findViewById(R.id.player_view_album)).setText(String.format("Album: %s",md.albumTitle));
        ((TextView)findViewById(R.id.player_view_artist)).setText(String.format("Track: %s",md.title));
    }

    @Override
    public void onEvents(Player player, Player.Events events) {
        if ( events.contains(EVENT_TRACKS_CHANGED) ) {
            MediaItem mediaItem = player.getCurrentMediaItem();
            Log.d("MusicExplorer",mediaItem.localConfiguration.uri.toString());
            properties.setCurrentFile(mediaItem.localConfiguration.uri.toString());
        }
        Log.d("PLAY",events.toString());
    }

    @Override
    public void onMediaItemTransition(MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {

        return;
    }
}
