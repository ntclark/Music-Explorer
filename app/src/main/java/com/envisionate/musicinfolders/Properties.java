package com.envisionate.musicinfolders;

import static com.envisionate.musicinfolders.Globals.theMusicExplorer;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.util.Stack;
import java.util.UUID;

public class Properties {

    private static final int AUTO_DISPLAY_COLUMNS = 5;
    private String rootFolder = null;
    private String rootFolderName = null;
    private String folderParents = null;
    private String uuid = null;
    private int folderViewWidth = 0;
    private int folderViewHeight = 0;
    private  int fileViewWidth = 0;
    private int autoDisplayColumns = AUTO_DISPLAY_COLUMNS;
    public Stack<String> parentList = null;

    private DocumentFile currentFolder = null;
    private DocumentFile currentFile = null;
    private DocumentFile lastPlayedFile = null;
    private Boolean resumePlayOnStart = true;
    private long currentTrackMS = 0;

    private static SharedPreferences preferences = null;

    Properties(SharedPreferences preferences) {
        this.preferences = preferences;
        rootFolder = preferences.getString("rootFolderName","<click Browse>");
        rootFolderName = theMusicExplorer.uriToName(Uri.parse(rootFolder));
        folderViewWidth = preferences.getInt("folderViewWidth",0);
        folderViewHeight = preferences.getInt("folderViewHeight",0);
        fileViewWidth = preferences.getInt("fileViewWidth",0);
        folderParents = preferences.getString("folderParents",null);
        if ( "".equals(folderParents) )
            folderParents = null;
        parentList = new Stack<String>();
        if ( ! ( null == folderParents ) )
            for ( String s : folderParents.split("\\{") )
                parentList.push(s);

        try {

            try {
                currentFolder = DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(preferences.getString("currentFolder",null)));
            } catch ( Exception ex ) {
                currentFolder = null;
            }

            if ( null == currentFolder )
                currentFolder = DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(rootFolder));

            try {
                currentFile = DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(preferences.getString("currentFile",null)));
            } catch ( Exception ex1 ) {
                currentFile = null;
            }

            try {
                lastPlayedFile = DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(preferences.getString("lastPlayedFile",null)));
            } catch ( Exception ex1 ) {
                lastPlayedFile = null;
            }

        } catch ( Exception ex ) {
        }

        autoDisplayColumns = preferences.getInt("autoDisplayColumns",AUTO_DISPLAY_COLUMNS);
        resumePlayOnStart = preferences.getBoolean("resumePlayOnStart",resumePlayOnStart);
        currentTrackMS = preferences.getLong("currentTrackMS",-1);

        uuid = preferences.getString("uuid", UUID.randomUUID().toString());

    }

    public void resetPreferences() {
        rootFolder = "<click Browse>";
        rootFolderName = theMusicExplorer.uriToName(Uri.parse(rootFolder));
        folderViewWidth = 0;
        folderViewHeight = 0;
        fileViewWidth = 0;
        folderParents = null;
        parentList.clear();
        autoDisplayColumns = AUTO_DISPLAY_COLUMNS;
        currentFolder = null;
        currentFile = null;
        lastPlayedFile = null;
        resumePlayOnStart = true;
        currentTrackMS = -1;
        uuid = UUID.randomUUID().toString();
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("rootFolderName", rootFolder);
        editor.putInt("folderViewWidth",folderViewWidth);
        editor.putInt("folderViewHeight",folderViewHeight);
        editor.putInt("fileViewWidth",fileViewWidth);
        folderParents  = "";
        for ( String s : parentList )
            folderParents += s + "{";
        if ( 0 < parentList.size() )
            folderParents = folderParents.substring(0,folderParents.length() - 1);
        editor.putString("folderParents",folderParents);
        editor.putInt("autoDisplayColumns",autoDisplayColumns);
        editor.putString("currentFolder",null == currentFolder ? null : currentFolder.getUri().toString());
        editor.putString("currentFile",null == currentFile ? null : currentFile.getUri().toString());
        editor.putString("lastPlayedFile",null == lastPlayedFile ? null : lastPlayedFile.getUri().toString());
        editor.putBoolean("resumePlayOnStart",resumePlayOnStart);
        editor.putLong("currentTrackMS",currentTrackMS);

        editor.putString("uuid",uuid);

        editor.commit();
    }

    public void setRootFolder(Uri uri) {
        rootFolder = uri.toString();
        rootFolderName = theMusicExplorer.uriToName(uri);
        savePreferences();
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public String getRootFolderName() {
        return rootFolderName;
    }

    public int getAutoDisplayColumns() {
        return autoDisplayColumns;
    }

    public void setAutoDisplayColumns(int v) {
        autoDisplayColumns = v;
        savePreferences();
    }

    public void setFolderViewWidth(int v) {
        folderViewWidth = v;
    }

    public int getFolderViewWidth() {
        return folderViewWidth;
    }

    public void setFolderViewHeight(int v) {
        folderViewHeight = v;
    }

    public int getFolderViewHeight() {
        return folderViewHeight;
    }

    public void setFileViewWidth(int v) {
        fileViewWidth = v;
    }

    public int getFileViewWidth() {
        return fileViewWidth;
    }

    public void setCurrentFolder(DocumentFile theFolder) {
        currentFolder = theFolder;
        savePreferences();
    }

    public DocumentFile getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFile(String s) {
        setCurrentFile(DocumentFile.fromTreeUri(theMusicExplorer,Uri.parse(s)));
    }

    public void setCurrentFile(DocumentFile theFile) {
        currentFile = theFile;
        savePreferences();
    }

    public DocumentFile getCurrentFile() {
        return currentFile;
    }

    public void setLastPlayedFile(DocumentFile theFile) {
        lastPlayedFile = theFile;
        savePreferences();
    }

    public DocumentFile getLastPlayedFile() {
        return lastPlayedFile;
    }

    public void setResumePlayOnStart(Boolean v) {
        resumePlayOnStart = v;
        savePreferences();
    }

    public Boolean getResumePlayOnStart() {
        return resumePlayOnStart;
    }

    public void setCurrentTrackMS(long v) {
        currentTrackMS = v;
    }

    public long getCurrentTrackMS() {
        if (  null == lastPlayedFile )
            currentTrackMS = -1;
        else if ( ! lastPlayedFile.getName().equals(currentFile.getName()) )
            currentTrackMS = -1;
        return currentTrackMS;
    }

}
