package com.envisionate.carservice;

import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.SessionInfo;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.validation.HostValidator;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;
import com.envisionate.musicexplorer.Globals;

import org.jspecify.annotations.NonNull;

public class CarService extends CarAppService {

    public class CarSession extends Session {

        @OptIn(markerClass = ExperimentalCarApi.class)
        public Screen onCreateScreen(Intent theIntent) {
            Globals.currentScreen = null;
            if ( ! ( null == theMusicExplorer ) && ! ( null == theMusicPlayer ) ) {
                CarPlayerScreen carPlayerScreen = new CarPlayerScreen(getCarContext());
                theMusicPlayer.addListener(carPlayerScreen);
                if ( ! ( null == properties.getCurrentFolder() && ! ( null == properties.getCurrentFile() ) ) ) {
                    MediaMetadata md = new MediaMetadata.Builder()
                            .setAlbumTitle(properties.getCurrentFolder().getName())
                            .setTitle(properties.getCurrentFile().getName())
                            .build();
                    carPlayerScreen.onMediaMetadataChanged(md);
                }
                theMusicPlayer.play();
                return Globals.currentScreen = carPlayerScreen;
            }
            return Globals.currentScreen = new CarEntitiesScreen(getCarContext());
        }

    }

    public CarService() {
        super();
        Log.d("MusicExplorer","The CarService constructor is called");
    }


    @Override
    public @NonNull HostValidator createHostValidator() {
        Log.d("MusicExplorer","createHostValidator called");
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @Override
    public Session onCreateSession(SessionInfo sessionInfo) {
        Log.d("MusicExplorer","onCreateSession is called");
        return new CarSession();
    }

}
