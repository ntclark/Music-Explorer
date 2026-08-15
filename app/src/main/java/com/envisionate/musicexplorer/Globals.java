package com.envisionate.musicexplorer;

import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.media3.common.Player;

import com.envisionate.carservice.CarService;
import com.envisionate.musicexplorer.interfaces.IMusicExplorer;

public final class Globals {

    public static MusicExplorer theMusicExplorer = null;
    public static IMusicExplorer theMusicExplorerInterface = null;
    public static Play theMusicPlayer = null;

    public static Player.Listener currentPlayerListener = null;

    public static Properties properties = null;

    public static Screen currentAutoScreen = null;
    public static CarService currentCarService = null;
    public static Session currentAutoSession = null;

}
