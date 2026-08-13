package com.envisionate.musicexplorer;

import static android.os.Looper.getMainLooper;
import static androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED;
import static androidx.media3.common.Player.EVENT_TIMELINE_CHANGED;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicExplorerInterface;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Measure;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.carservice.screen.CarEntitiesScreen;
import com.envisionate.musicexplorer.interfaces.IMusicExplorer;

import java.util.ArrayList;
import java.util.Comparator;

public class MusicExplorer extends AppCompatActivity implements Player.Listener {

    public class ItemViewAdapter extends ArrayAdapter<itemModel> {

        protected int itemTextViewId = 0;
        protected int itemImageViewId = 0;
        protected int itemLayoutId = 0;

        public ItemViewAdapter(Context context, ArrayList<itemModel> list) {
            super(context, 0, list);
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

            textView.setText(model.getName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    theMusicExplorerInterface.onItemClicked(((itemModel)v.getTag()).getDocumentFile(),null,null);
                }
            });

            itemView.setTag(model);

            return itemView;
        }

    }

    public class FolderViewAdapter extends ItemViewAdapter {

        public FolderViewAdapter(Context context, ArrayList<itemModel> list) {
            super(context,  list);
            itemTextViewId = R.id.folder_text_view;
            itemImageViewId = R.id.folder_image_view;
            itemLayoutId = R.layout.folder_view;
        }

    }

    public class TrackViewAdapter extends ItemViewAdapter {

        public TrackViewAdapter(Context context, ArrayList<itemModel> list) {
            super(context,  list);
            itemTextViewId = R.id.file_text_view;
            itemImageViewId = R.id.file_image_view;
            itemLayoutId = R.layout.track_view;
        }

    }

    public static View homeView = null;
    public static GridView foldersGridView = null;
    public static GridView tracksGridView = null;
    private static FrameLayout filesFrameLayout = null;

    ArrayList<itemModel> theFolders = null;
    ArrayList<itemModel> theTracks = null;

    private static int homeViewHeight = 0;
    private static int foldersViewWidth = 0;
    private static int foldersViewHeight = 0;
    private static int filesViewWidth = 0;
    private static int filesViewHeight = 0;

    public static Properties properties = null;

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

        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();

        homeView = findViewById(R.id.activity_main);

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
        if ( properties.getResumePlayOnStart() && null == theMusicPlayer ) {
            if ( ! ( null == properties.getCurrentFile() ) ) {
                theMusicExplorerInterface.playCurrentTrack(this,null);
                playStarted = true;
            }
        }

        if ( ! playStarted ) {
            String startedByCar = getIntent().getStringExtra("StartedByCar");
            if ( ( ! ( null == startedByCar ) && "true".equals(startedByCar) ) || ! ( null == CarEntitiesScreen.getWaitingScreen() ) ) {
                Intent intent = new Intent("com.envisionate.musicexplorer.STARTED");
                intent.setPackage("com.envisionate.musicexplorer");
                getApplicationContext().sendBroadcast(intent);
            }
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
            this.finishAndRemoveTask();
            return true;
        }
        return false;
    }

    public void displayFoldersAndFiles(DocumentFile innerPath) {

        DocumentFile theRoot = null;

        if ( null == innerPath ) {
            try {
                theRoot = DocumentFile.fromTreeUri(this, Uri.parse(properties.getRootFolder()));
            } catch (Exception ex) {
                properties.resetPreferences();
                startActivity(new Intent(this, Settings.class));
                return;
            }
        }

        DocumentFile [] itemArray = null;

        if ( null == innerPath )
            itemArray = theRoot.listFiles();
        else
            itemArray = innerPath.listFiles();

        if ( 0 == itemArray.length && null == properties.getRootFolder() ) {
            properties.resetPreferences();
            startActivity(new Intent(this, Settings.class));
            return;
        }

        properties.setCurrentFolder(innerPath);

        FolderViewAdapter foldersViewAdapter = (FolderViewAdapter)foldersGridView.getAdapter();

        if ( ! ( null == foldersViewAdapter ) ) {
            foldersViewAdapter.clear();
            foldersViewAdapter.notifyDataSetChanged();
        }

        if ( null == theFolders )
            theFolders = new ArrayList<itemModel>();
        else
            theFolders.clear();

        TrackViewAdapter tracksViewAdapter = (TrackViewAdapter) tracksGridView.getAdapter();

        if ( ! ( null == tracksViewAdapter ) ) {
            tracksViewAdapter.clear();
            tracksViewAdapter.notifyDataSetChanged();
        }

        if ( null == theTracks)
            theTracks = new ArrayList<itemModel>();

        for ( DocumentFile df : itemArray ) {
            String fn = folderName(df.getName());
            if ( fn.startsWith(".") )
                continue;
            if ( df.isDirectory() )
                theFolders.add(new itemModel(fn, df));
            else
                theTracks.add(new itemModel(fn,df));
        }

        theFolders.sort(new Comparator<itemModel>() {
            @Override
            public int compare(itemModel o1, itemModel o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        actionBar.setDisplayHomeAsUpEnabled(null == innerPath || uriToName(innerPath.getUri()).equals(properties.getRootFolderName()) ? false : true);

        if ( null == foldersViewAdapter ) {
            foldersViewAdapter = new FolderViewAdapter(this, theFolders);
            foldersGridView.setAdapter(foldersViewAdapter);
        }

        foldersViewAdapter.notifyDataSetChanged();

        if ( null == tracksViewAdapter ) {
            tracksViewAdapter = new TrackViewAdapter(this, theTracks);
            tracksGridView.setAdapter(tracksViewAdapter);
        }

        tracksViewAdapter.notifyDataSetChanged();

        foldersGridView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                layoutFolders();
            }
        });

    }

    public void layoutFolders() {

        foldersViewWidth = foldersGridView.getWidth();

        if ( 0 < foldersGridView.getChildCount() ) {
            ViewGroup folderChild = (ViewGroup)foldersGridView.getChildAt(0);
            properties.setFolderViewWidth(folderChild.getWidth() + folderChild.getPaddingTop() + folderChild.getPaddingBottom());
            properties.setFolderViewHeight(folderChild.getHeight() + folderChild.getPaddingLeft() + folderChild.getPaddingRight());
        }

        int columnCount = 1;
        if ( 0 < properties.getFolderViewWidth() )
            columnCount = foldersViewWidth / properties.getFolderViewWidth();
        int rowCount = Math.max(1,theFolders.size() / columnCount);
        if ( rowCount * columnCount < theFolders.size() )
            rowCount += 1;

        if ( theFolders.isEmpty() )
            foldersViewHeight = 0;
        else
            foldersViewHeight = Math.min(rowCount * properties.getFolderViewHeight(),3 * homeViewHeight / 4);

        foldersGridView.setNumColumns(columnCount);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(foldersViewWidth,foldersViewHeight);
        foldersGridView.setLayoutParams(params);

        filesFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutFiles(this);
            }
        });
    }

    private void layoutFiles(ViewTreeObserver.OnGlobalLayoutListener listener) {

        filesFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(listener);

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

        findViewById(R.id.no_files_view).setVisibility(0 == theTracks.size() ? View.VISIBLE : View.INVISIBLE);

    }


    public String folderName(String folderPath) {
        if ( -1 == folderPath.lastIndexOf(':') )
            return folderPath;
        return folderPath.substring(folderPath.lastIndexOf(':') + 1);
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
