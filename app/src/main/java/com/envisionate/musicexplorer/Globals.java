package com.envisionate.musicexplorer;

import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.Player;

import com.envisionate.musicexplorer.interfaces.IMusicExplorer;

public final class Globals {

    public static MusicExplorer theMusicExplorer = null;
    public static IMusicExplorer theMusicExplorerInterface = null;
    public static Play theMusicPlayer = null;

    public static Player.Listener currentPlayerListener = null;

    public static final int FOLDER_WIDTH = 448;
    public static final int FOLDER_LEFT_PADDING = 16;

    public static final int FOLDER_HEIGHT = 448;
    public static final int TEXT_HEIGHT = 96;
}
