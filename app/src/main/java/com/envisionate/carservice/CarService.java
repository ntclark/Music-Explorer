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

    public class CarSession extends Session {

        public Screen onCreateScreen(Intent theIntent) {

            Globals.currentAutoScreen = null;

            if ( ! ( null == theMusicExplorer ) && ! ( null == theMusicPlayer ) ) {
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

    }

    public CarService() {
        super();
        currentCarService = this;
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
