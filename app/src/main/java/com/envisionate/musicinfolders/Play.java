package com.envisionate.musicinfolders;

import static android.view.View.INVISIBLE;
import static androidx.media3.common.Player.EVENT_PLAYER_ERROR;
import static androidx.media3.common.Player.EVENT_TRACKS_CHANGED;
import static com.envisionate.musicinfolders.Globals.PLAYER_TRACK_QUERY_DELAY;
import static com.envisionate.musicinfolders.Globals.currentAutoScreen;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicExplorerInterface;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;

import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.envisionate.musicinfolders.interfaces.IMusicExplorerPlay;

public class Play extends AppCompatActivity implements Player.Listener {

    private ExoPlayer player = null;
    private PlayerView playerView = null;

    private DocumentFile thePlayingItem = null;

    private Runnable trackPositionQuery = null;
    private Boolean stopPositionQuery = false;
    private IMusicExplorerPlay trackMSListener = null;

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

        trackPositionQuery = new Runnable() {
            @Override
            public void run() {
                properties.setCurrentTrackMS(player.getCurrentPosition());
                if ( ! ( null == trackMSListener ) )
                    trackMSListener.onPlayTrackMSChanged(properties.getCurrentTrackMS());
                if ( ! ( theMusicPlayer.stopPositionQuery ) )
                    new Handler(getMainLooper()).postDelayed(trackPositionQuery,PLAYER_TRACK_QUERY_DELAY);
            }
        };

        player.prepare();

        if ( -1 < properties.getCurrentTrackMS() )
            player.seekTo(properties.getCurrentTrackMS());

        player.play();

        String startedByCar = theMusicExplorer.getIntent().getStringExtra("StartedByCar");

        if ( ! ( null == startedByCar ) && "true".equals(startedByCar) ) {
            Util.broadcast(this,"com.envisionate.musicinfolders.STARTED_WITH_PLAY");
            theMusicExplorer.getIntent().putExtra("StartedByCar","");
        }

        String clickIsFromAuto = getIntent().getStringExtra("ClickIsFromAuto");

        if ( ! ( null == clickIsFromAuto ) ) {
            if ( "false".equals(clickIsFromAuto) )
                Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.PLAY_NOTIFY");
            else
                Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.PLAY_NOTIFY_UPDATE_TRACK_INFO");
        }

        properties.setLastPlayedFile(properties.getCurrentFile());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.exit_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == android.R.id.home ) {
            theMusicExplorerInterface.gotoParent();
            return true;
        }
        if ( item.getItemId() == R.id.exit ) {
            if ( ! ( null == currentAutoScreen ) ) {
                Util.broadcast(this,"com.envisionate.musicinfolders.STOP_REQUESTED");
                return true;
            }
            stop();
            theMusicExplorer.finishAndRemoveTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void pause() {
        player.pause();
    }

    public void previous() {
        player.seekToPrevious();
        Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.PLAY_NOTIFY_UPDATE_TRACK_INFO");
    }

    public void play() {
        player.play();
    }

    public void next() {
        player.seekToNext();
        Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.PLAY_NOTIFY_UPDATE_TRACK_INFO");
    }

    public void stop() {
        trackMSListener = null;
        player.stop();
        finish();
    }

    public void addListener(Player.Listener obj) {
        player.addListener(obj);
    }

    public void addListener(IMusicExplorerPlay obj) {
        trackMSListener = obj;
    }

    public long getTrackDuration() {
        return player.getDuration();
    }

   @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if ( isPlaying ) {
            stopPositionQuery = false;
            new Handler(getMainLooper()).postDelayed(trackPositionQuery,PLAYER_TRACK_QUERY_DELAY);
        } else
            stopPositionQuery = true;
    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata md) {
        ((TextView)findViewById(R.id.player_view_album)).setText(String.format("Album: %s",md.albumTitle));
        ((TextView)findViewById(R.id.player_view_artist)).setText(String.format("Track: %s",md.title));
    }

    @Override
    public void onEvents(Player player, Player.Events events) {
        MediaItem mediaItem = player.getCurrentMediaItem();
        if ( events.contains(EVENT_TRACKS_CHANGED) ) {
            properties.setCurrentFile(mediaItem.localConfiguration.uri.toString());
        } else if ( events.contains(EVENT_PLAYER_ERROR) ) {
            ((TextView)findViewById(R.id.player_view_folder)).setVisibility(INVISIBLE);
            String s = DocumentFile.fromTreeUri(this,mediaItem.localConfiguration.uri).getName();
            ((TextView)findViewById(R.id.player_view_album)).setText(String.format("Track: %s",s));
            ((TextView)findViewById(R.id.player_view_artist)).setText(String.format(getString(R.string.track_error)));
        }
        Log.d("MusicExplorer",events.toString());
    }

    @Override
    public void onMediaItemTransition(MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
        return;
    }
}
