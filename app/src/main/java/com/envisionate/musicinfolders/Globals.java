package com.envisionate.musicinfolders;

import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.media3.common.Player;

import com.envisionate.carservice.CarService;
import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.carservice.screen.CarPlayerScreen;
import com.envisionate.musicinfolders.interfaces.IMusicExplorer;

@ExperimentalCarApi
public final class Globals {

    public static TrackLaunchedActivities trackLaunchedActivities = null;

    public static MusicExplorer theMusicExplorer = null;
    public static IMusicExplorer theMusicExplorerInterface = null;
    public static Play theMusicPlayer = null;

    public static Player.Listener currentPlayerListener = null;

    public static Properties properties = null;

    public static final int ANDROID_AUTO_DELAY = 250;
    public static final int PLAYER_TRACK_QUERY_DELAY = 1000;
    public static final int ANDROID_AUTO_ICON_SIDE = 228;
    public static final Boolean AUTO_DISPLAY_FOLDERS = false;

    public static Screen currentAutoScreen = null;
    public static CarEntitiesScreen currentAutoEntitiesScreen = null;
    public static CarPlayerScreen currentAutoPlayerScreen = null;

    public static CarService currentCarService = null;
    public static Session currentAutoSession = null;

    public static Util theUtilities = new Util();

}
