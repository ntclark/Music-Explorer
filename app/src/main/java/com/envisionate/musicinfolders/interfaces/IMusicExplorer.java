package com.envisionate.musicinfolders.interfaces;

import static android.os.Looper.getMainLooper;
import static com.envisionate.musicinfolders.Globals.currentAutoScreen;
import static com.envisionate.musicinfolders.Globals.currentPlayerListener;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;
import static com.envisionate.musicinfolders.Globals.trackLaunchedActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;

import androidx.car.app.CarToast;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.Player;

import com.envisionate.musicinfolders.Play;
import com.envisionate.musicinfolders.Util;

public class IMusicExplorer {

     public void onItemClicked(DocumentFile thePath,Player.Listener theListener,Runnable onReady,Boolean fromAuto) {
        if ( thePath.isDirectory() ) {
            theMusicExplorer.setParentOf(properties.getCurrentFolder());
            if ( ! ( null == currentAutoScreen ) )
                CarToast.makeText(currentAutoScreen.getCarContext(),"Navigating",CarToast.LENGTH_SHORT).show();
            properties.setCurrentFolder(thePath);
            theMusicExplorer.displayFoldersAndFiles(thePath);
            if ( ! ( null == onReady ) )
                onReady.run();
            return;
        }
        properties.setCurrentFile(thePath);
        playCurrentTrack(theListener,onReady,fromAuto);
    }


    public void gotoParent() {
        if ( ! ( null == currentAutoScreen ) )
            CarToast.makeText(currentAutoScreen.getCarContext(),"Navigating",CarToast.LENGTH_SHORT).show();
        if ( "Play".equals(trackLaunchedActivities.getLastLaunchedActivity()) )  {
            theMusicPlayer.stop();
            theMusicExplorer.displayFoldersAndFiles(properties.getCurrentFolder());
        } else {
            DocumentFile df = theMusicExplorer.getParentOf(properties.getCurrentFolder(),true);
            theMusicExplorer.displayFoldersAndFiles(df);
            properties.setCurrentFolder(df);
        }
        properties.setCurrentFile((DocumentFile)null);
    }

    public void previous() {
        theMusicPlayer.previous();
    }

    public void pause() {
        theMusicPlayer.pause();
    }

    public void play() {
        theMusicPlayer.play();
    }

    public void next() {
        theMusicPlayer.next();
    }

    public void stopPlay() {
        theMusicPlayer.stop();
    }

    public void playCurrentTrack(Player.Listener theListener,Runnable onReady,Boolean fromAuto) {
        currentPlayerListener = theListener;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Intent startPlayer = new Intent(theMusicExplorer,Play.class);
                startPlayer.putExtra("ClickIsFromAuto",fromAuto ? "true" : "false");
                theMusicExplorer.startActivity(startPlayer);
                if ( ! ( null == onReady ) )
                    onReady.run();
            }
        });
    }

    public Boolean isRoot() {
        if ( null == properties.getCurrentFolder() )
            return true;
        if ( null == theMusicExplorer.getParentOf(properties.getCurrentFolder(),false) )
            return true;
        return false;
    }

    public Boolean hasRoot() {
        return ! ( null == properties.getRootFolder() ) && ! "<click Browse>".equals(properties.getRootFolder());
    }

    public DocumentFile getPath(String folderName) {
        try {
            return DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(folderName));
        } catch ( Exception ex ) {
            return null;
        }
    }

    public void setCurrentTrack(DocumentFile theTrack) {
        properties.setCurrentFile(theTrack);
    }

    public Player.Listener getCurrentPlayerListener() {
        return currentPlayerListener;
    }

    public Bitmap getFolderImage(DocumentFile theFolder) {
        return Util.getFolderImage(theMusicExplorer,theFolder);
    }

}
