package com.envisionate.carservice;

import static com.envisionate.musicinfolders.Globals.currentAutoScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoSession;
import static com.envisionate.musicinfolders.Globals.currentCarService;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;

import android.content.Intent;
import android.media.session.MediaSession;

import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.SessionInfo;
import androidx.car.app.validation.HostValidator;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;
import com.envisionate.musicinfolders.Globals;
import com.envisionate.musicinfolders.interfaces.IMusicExplorerPlay;

import org.jspecify.annotations.NonNull;

public class CarService extends CarAppService {

    protected MediaSession theMediaSession = null;

    public class CarSession extends Session {

        public Screen onCreateScreen(Intent theIntent) {

            Globals.currentAutoScreen = null;

            if ( ! ( null == theMusicExplorer ) && ! ( null == theMusicPlayer ) ) {
                // These are for implementing MediaPlaybackTemplate - which I think is not supported yet anyway
                //MediaPlaybackScreen carPlayerScreen = new MediaPlaybackScreen(getCarContext());
                //return currentAutoScreen = carPlayerScreen;
                CarPlayerScreen carPlayerScreen = new CarPlayerScreen(getCarContext());
                theMusicPlayer.addListener((Player.Listener)carPlayerScreen);
                theMusicPlayer.addListener((IMusicExplorerPlay)carPlayerScreen);
                if ( ! ( null == properties.getCurrentFolder() ) && ! ( null == properties.getCurrentFile() ) ) {
                    MediaMetadata md = new MediaMetadata.Builder()
                            .setAlbumTitle(properties.getCurrentFolder().getName())
                            .setTitle(properties.getCurrentFile().getName())
                            .build();
                    carPlayerScreen.onMediaMetadataChanged(md);
                }
                theMusicPlayer.play();
                return currentAutoScreen = carPlayerScreen;

            }
            return currentAutoScreen = new CarEntitiesScreen(getCarContext());
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onCreate() {
            // These are for implementing MediaPlaybackTemplate - which I think is not supported yet anyway
            //MediaPlaybackManager mediaPlaybackManager = (MediaPlaybackManager)getCarContext().getCarService(CarContext.MEDIA_PLAYBACK_SERVICE);
            //MediaSession.Token token = theMediaSession.getSessionToken();
            //MediaSessionCompat.Token token2 = MediaSessionCompat.Token.fromToken(token);
            //mediaPlaybackManager.registerMediaPlaybackToken(token2);
        }
    }

    public CarService() {
        super();
        currentCarService = this;
        // These are for implementing MediaPlaybackTemplate - which I think is not supported yet anyway
        //MediaSession theMediaSession = new MediaSession(getApplicationContext(),"Music Explorer");
    }

    @Override
    public @NonNull HostValidator createHostValidator() {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }


    @Override
    public Session onCreateSession(SessionInfo sessionInfo) {
        return currentAutoSession = new CarSession();
    }

}
