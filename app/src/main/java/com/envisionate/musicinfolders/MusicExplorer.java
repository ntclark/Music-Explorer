package com.envisionate.musicinfolders;

import static com.envisionate.musicinfolders.Globals.ANDROID_AUTO_DELAY;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicExplorerInterface;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;
import static com.envisionate.musicinfolders.Globals.theUtilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.musicinfolders.interfaces.IMusicExplorer;

import java.util.ArrayList;

public class MusicExplorer extends AppCompatActivity implements Player.Listener {

    public class ItemViewAdapter extends ArrayAdapter<itemModel> {

        private int itemTextViewId = 0;
        private int itemImageViewId = 0;
        private int itemLayoutId = 0;

        public ItemViewAdapter(Context context, ArrayList<itemModel> list,int textViewId,int imageViewId,int layoutId) {
            super(context, 0, list);
            itemTextViewId = textViewId;
            itemImageViewId = imageViewId;
            itemLayoutId = layoutId;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent) {

            View itemView = convertView;
            if ( null == itemView )
                itemView = LayoutInflater.from(getContext()).inflate(itemLayoutId, parent, false);

            itemModel model = getItem(position);

            model.setImageView(itemView.findViewById(itemImageViewId));
            model.setTextView(itemView.findViewById(itemTextViewId));

            TextView textView = itemView.findViewById(itemTextViewId);

            String s = model.getName();
            if ( -1 < s.lastIndexOf('.') )
                s = s.substring(0,s.lastIndexOf('.'));

            textView.setText(s);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    theMusicExplorerInterface.onItemClicked(((itemModel)v.getTag()).getDocumentFile(),null,null,false);
                }
            });

            itemView.setTag(model);

            return itemView;
        }

    }

    public class FolderViewAdapter extends ItemViewAdapter {
        public FolderViewAdapter(Context context, ArrayList<itemModel> list) {
            super(context,  list,R.id.folder_text_view,R.id.folder_image_view,R.layout.folder_view);
        }
    }

    public class TrackViewAdapter extends ItemViewAdapter {
        public TrackViewAdapter(Context context, ArrayList<itemModel> list) {
            super(context,  list,R.id.file_text_view,R.id.file_image_view,R.layout.track_view);
        }
    }

    public View homeView = null;
    public GridView foldersGridView = null;
    public GridView tracksGridView = null;
    private FrameLayout filesFrameLayout = null;

    private ArrayList<itemModel> theFolderItems = new ArrayList<itemModel>();
    private ArrayList<itemModel> theTrackItems = new ArrayList<itemModel>();

    private static int homeViewHeight = 0;
    private static int foldersViewWidth = 0;
    private static int foldersViewHeight = 0;
    private static int filesViewWidth = 0;
    private static int filesViewHeight = 0;
    private static Boolean isViewLayedOut = false;

    private ActionBar actionBar = null;

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        theMusicExplorer = this;
        theMusicExplorerInterface = new IMusicExplorer();

        if ( null == properties )
            properties = new Properties(getPreferences(MODE_PRIVATE));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.musicexplorer_main);

        actionBar = getSupportActionBar();

        homeView = findViewById(R.id.musicexplorer_main);
        foldersGridView = findViewById(R.id.folders_view_grid);
        filesFrameLayout = findViewById(R.id.files_view_frame);
        tracksGridView = findViewById(R.id.files_view_grid);

        homeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                homeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                homeViewHeight = homeView.getHeight() - actionBar.getHeight();
            }
        });

        Boolean playStarted = false;
        if ( properties.getResumePlayOnStart() ) {
            if ( ! ( null == properties.getCurrentFile() ) ) {
                theMusicExplorerInterface.playCurrentTrack(this,null,false);
                playStarted = true;
            }
        }

       if ( ! playStarted ) {
            String startedByCar = getIntent().getStringExtra("StartedByCar");
            if ( ( ! ( null == startedByCar ) && "true".equals(startedByCar) ) ) //|| ! ( null == CarEntitiesScreen.getWaitingScreen() ) )
                Util.broadcast(this,"com.envisionate.musicinfolders.STARTED");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        displayFoldersAndFiles(properties.getCurrentFolder());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if ( ! ( null == theMusicPlayer ) )
            theMusicPlayer.finish();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.common, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if ( R.id.action_settings == item.getItemId() ) {
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        if ( item.getItemId() == android.R.id.home ) {
            theMusicExplorerInterface.gotoParent();
            return true;
        }
        if ( R.id.exit == item.getItemId() ) {
            Util.broadcast(this,"com.envisionate.musicinfolders.STOP_REQUESTED");
            return true;
        }
        return false;
    }

    public void displayFoldersAndFiles(DocumentFile innerPath) {

        filesAndFolders theEntities = theUtilities.getCurrentFilesAndFolders(innerPath);

        if ( null == theEntities ) {
            properties.resetPreferences();
            startActivity(new Intent(this, Settings.class));
            return;
        }

        FolderViewAdapter foldersViewAdapter = (FolderViewAdapter)foldersGridView.getAdapter();
        if ( ! ( null == foldersViewAdapter ) ) {
            foldersViewAdapter.clear();
            foldersViewAdapter.notifyDataSetChanged();
        }

        theFolderItems.clear();

        TrackViewAdapter tracksViewAdapter = (TrackViewAdapter)tracksGridView.getAdapter();
        if ( ! ( null == tracksViewAdapter ) ) {
            tracksViewAdapter.clear();
            tracksViewAdapter.notifyDataSetChanged();
        }

        theTrackItems.clear();

        for ( DocumentFile df : theEntities.getFolders() ) {
            String fn = folderName(df.getName());
            theFolderItems.add(new itemModel(fn, df));
        }

        for ( DocumentFile df : theEntities.getFiles() )
            theTrackItems.add(new itemModel(df.getName(),df));

        actionBar.setDisplayHomeAsUpEnabled(null == innerPath || uriToName(innerPath.getUri()).equals(properties.getRootFolderName()) ? false : true);

        if ( null == foldersViewAdapter ) {
            foldersViewAdapter = new FolderViewAdapter(this,theFolderItems);
            foldersGridView.setAdapter(foldersViewAdapter);
        }

        foldersViewAdapter.notifyDataSetChanged();

        if ( null == tracksViewAdapter ) {
            tracksViewAdapter = new TrackViewAdapter(this,theTrackItems);
            tracksGridView.setAdapter(tracksViewAdapter);
        }

        tracksViewAdapter.notifyDataSetChanged();

        isViewLayedOut = false;

        homeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                homeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layoutFoldersAndFiles();
            }
        });

    }

    public void layoutFoldersAndFiles() {

        foldersViewWidth = foldersGridView.getWidth();

        if ( 0 < foldersGridView.getChildCount() ) {
            ViewGroup folderChild = (ViewGroup)foldersGridView.getChildAt(0);
            properties.setFolderViewHeight(folderChild.getHeight() + folderChild.getPaddingTop() + folderChild.getPaddingBottom());
            properties.setFolderViewWidth(folderChild.getWidth() + folderChild.getPaddingLeft() + folderChild.getPaddingRight());
        }

        int columnCount = 1;
        if ( 0 < properties.getFolderViewWidth() )
            columnCount = foldersViewWidth / properties.getFolderViewWidth();
        int rowCount = Math.max(1, theFolderItems.size() / columnCount);
        if ( rowCount * columnCount < theFolderItems.size() )
            rowCount += 1;

        if ( theFolderItems.isEmpty() )
            foldersViewHeight = 0;
        else
            foldersViewHeight = Math.min(rowCount * properties.getFolderViewHeight(),homeViewHeight);

        foldersGridView.setNumColumns(columnCount);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(foldersViewWidth,foldersViewHeight);
        foldersGridView.setLayoutParams(params);

        layoutFiles();

        if ( ! isViewLayedOut && null == CarEntitiesScreen.getWaitingScreen() )
            Util.broadcastLater(this,"com.envisionate.musicinfolders.NAVIGATION_NOTIFY",ANDROID_AUTO_DELAY);

        isViewLayedOut = true;

    }

    private void layoutFiles() {

        filesViewWidth = filesFrameLayout.getWidth();

        if ( 0 < tracksGridView.getChildCount() ) {
            ViewGroup fileChild = (ViewGroup) tracksGridView.getChildAt(0);
            properties.setFileViewWidth(fileChild.getWidth() + fileChild.getPaddingTop() + fileChild.getPaddingBottom());
            int columnCount = filesViewWidth / properties.getFileViewWidth();
            tracksGridView.setNumColumns(columnCount);
        }

        filesViewHeight = homeViewHeight - foldersViewHeight;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(filesViewWidth, filesViewHeight);
        params.topMargin = foldersViewHeight;
        filesFrameLayout.setLayoutParams(params);

        findViewById(R.id.no_files_view).setVisibility(0 == theTrackItems.size() ? View.VISIBLE : View.INVISIBLE);

    }

    public String folderName(String folderPath) {
        if ( -1 == folderPath.lastIndexOf(':') )
            return folderPath;
        return folderPath.substring(folderPath.lastIndexOf(':') + 1);
    }

    public filesAndFolders getDisplayedFilesAndFolder() {

        if ( theFolderItems.isEmpty() && theTrackItems.isEmpty() )
            return null;

        ArrayList<DocumentFile> folders = new ArrayList<DocumentFile>();
        ArrayList<DocumentFile> files = new ArrayList<DocumentFile>();

        for ( itemModel im : theFolderItems )
            folders.add(im.getDocumentFile());

        for ( itemModel im : theTrackItems )
            files.add(im.getDocumentFile());

        return new filesAndFolders(folders,files);
    }

    public String uriToName(Uri theUri) {
        if ( null == theUri )
            return null;
        String [] pathNames = theUri.getPath().split(":");
        if ( 0 < pathNames.length )
            return pathNames[pathNames.length - 1];
        return null;
    }

    public void setParentOf(DocumentFile aFolder) {
        if ( null == aFolder )
            properties.parentList.push(Uri.parse(properties.getRootFolder()).toString());
        else
            properties.parentList.push(aFolder.getUri().toString());
    }

    public DocumentFile getParentOf(DocumentFile aFolder,Boolean isNavigation) {
        if ( null == aFolder )
            return null;
        if ( 0 == properties.parentList.size() )
            return null;
        return DocumentFile.fromTreeUri(this,Uri.parse(isNavigation ? properties.parentList.pop() : properties.parentList.peek()));
    }

    // Player.Listener methods:

    @Override
    public void onMediaMetadataChanged(MediaMetadata md) {
    }

    @Override
    public void onEvents(Player player, Player.Events events) {
    }
}
