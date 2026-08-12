package com.envisionate.carservice;

import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.car.app.CarAppService;

import androidx.car.app.IStartCarApp;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.SessionInfo;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.validation.HostValidator;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaMetadata;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;
import com.envisionate.musicexplorer.MusicExplorer;
import org.jspecify.annotations.NonNull;

public class CarService extends CarAppService {

    public class CarSession extends Session {

        private Lifecycle theLifeCycle = null;
        private Boolean isBound = false;

        public class CarLifecycleObserver implements LifecycleEventObserver {
            @Override
            public void onStateChanged(@androidx.annotation.NonNull LifecycleOwner lifecycleOwner, @androidx.annotation.NonNull Lifecycle.Event event) {
                Log.d("MusicExplorer ","onStateChanaged " + event.name());
            }
        }

        @OptIn(markerClass = ExperimentalCarApi.class)
        public Screen onCreateScreen(Intent theIntent) {
            Log.d("MusicExplorer","onCreateScreen is called");
            theLifeCycle = getLifecycle();
            theLifeCycle.addObserver(new CarLifecycleObserver());
            if ( ! ( null == theMusicExplorer ) ) {
                if ( ! ( null == theMusicPlayer ) ) {
                    CarPlayerScreen carPlayerScreen = new CarPlayerScreen(getCarContext());
                    theMusicPlayer.addListener(carPlayerScreen);
                    MediaMetadata md = new MediaMetadata.Builder()
                            .setAlbumTitle(properties.getCurrentFolder().getName())
                            .setTitle(properties.getCurrentFile().getName())
                            .build();
                    carPlayerScreen.onMediaMetadataChanged(md);
                    theMusicPlayer.play();
                    return carPlayerScreen;
                }
            }
            return new CarEntitiesScreen(getCarContext());
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
