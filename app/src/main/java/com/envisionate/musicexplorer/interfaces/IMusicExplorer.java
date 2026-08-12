package com.envisionate.musicexplorer.interfaces;

import static android.os.Looper.getMainLooper;

import static com.envisionate.musicexplorer.Globals.currentPlayerListener;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;
import static com.envisionate.musicexplorer.MusicExplorer.foldersGridView;
import static com.envisionate.musicexplorer.MusicExplorer.properties;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.Player;

import com.envisionate.musicexplorer.Globals;
import com.envisionate.musicexplorer.MusicExplorer;
import com.envisionate.musicexplorer.Play;
import com.envisionate.musicexplorer.Settings;
import com.envisionate.musicexplorer.Util;

import java.util.ArrayList;
import java.util.Comparator;

public class IMusicExplorer {

    public class filesAndFolders {

        public filesAndFolders(ArrayList<DocumentFile> tf, ArrayList<DocumentFile> tfiles) {
            theFolders = tf;
            theFiles = tfiles;
        }

        private ArrayList<DocumentFile> theFolders;
        private ArrayList<DocumentFile> theFiles;

        public ArrayList<DocumentFile> getFolders() {
            return theFolders;
        }

        public ArrayList<DocumentFile> getFiles() {
            return theFiles;
        }

    }

    public filesAndFolders getCurrentFilesAndFolders() {

        DocumentFile theRoot = null;

        if ( null == properties.getCurrentFolder() ) {
            try {
                theRoot = DocumentFile.fromTreeUri(theMusicExplorer, Uri.parse(properties.getRootFolder()));
            } catch ( Exception ex ) {
                return null;
            }
        }

        DocumentFile [] itemArray = null;

        if ( null == properties.getCurrentFolder() )
            itemArray = theRoot.listFiles();
        else
            itemArray = properties.getCurrentFolder().listFiles();

        if ( 0 == itemArray.length ) {
            if ( ! ( null == properties.getCurrentFolder() ) )
                return null;
            properties.resetPreferences();
            theMusicExplorer.startActivity(new Intent(theMusicExplorer, Settings.class));
            return null;
        }

        ArrayList<DocumentFile> folders = new ArrayList<DocumentFile>();
        ArrayList<DocumentFile> files = new ArrayList<DocumentFile>();

        for ( DocumentFile df : itemArray ) {
            String fn = theMusicExplorer.folderName(df.getName());
            if ( fn.startsWith(".") )
                continue;
            if ( df.isDirectory() )
                folders.add(df);
            else
                files.add(df);
        }

        folders.sort(new Comparator<DocumentFile>() {
            @Override
            public int compare(DocumentFile o1, DocumentFile o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return new filesAndFolders(folders,files);
    }


    public void onItemClicked(DocumentFile thePath, Player.Listener theListener,Runnable onReady) {

        if ( thePath.isDirectory() ) {
            theMusicExplorer.setParentOf(properties.getCurrentFolder());
            properties.setCurrentFolder(thePath);
            theMusicExplorer.displayFoldersAndFiles(thePath);
            if ( ! ( null == onReady ) )
                onReady.run();
            return;
        }

        properties.setCurrentFile(thePath);
        playCurrentTrack(theListener,onReady);

    }


    public void gotoParent() {
        if ( ! ( null == theMusicPlayer ) && theMusicPlayer.hasWindowFocus() ) {
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

    public void playCurrentTrack(Player.Listener theListener,Runnable onReady) {
        currentPlayerListener = theListener;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                theMusicExplorer.startActivity(new Intent(theMusicExplorer, Play.class));
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
        return ! ( null == properties.getRootFolder() );
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
