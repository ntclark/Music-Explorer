package com.envisionate.carservice.screen;

import static android.os.Looper.getMainLooper;
import static androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED;
import static androidx.media3.common.Player.EVENT_PLAYER_ERROR;
import static androidx.media3.common.Player.EVENT_TIMELINE_CHANGED;
import static com.envisionate.musicexplorer.Globals.currentAutoScreen;
import static com.envisionate.musicexplorer.Globals.properties;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;
import static com.envisionate.musicexplorer.Globals.theMusicExplorerInterface;
import static com.envisionate.musicexplorer.Globals.theMusicPlayer;

import android.os.Handler;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridSection;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.Header;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.RowSection;
import androidx.car.app.model.Section;
import androidx.car.app.model.SectionHeader;
import androidx.car.app.model.SectionedItemTemplate;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.carservice.CarService;
import com.envisionate.musicexplorer.R;
import com.envisionate.musicexplorer.Util;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@ExperimentalCarApi
public class CarPlayerScreen extends Screen implements Player.Listener {

    private List<Section<?>> sections = new ArrayList<Section<?>>();
    private RowSection.Builder rowSectionBuilder = new RowSection.Builder();
    //private RowSection.Builder progressBarRowSectionBuilder = new RowSection.Builder();
    private GridSection.Builder gridSectionBuilder = new GridSection.Builder();

    private SectionedItemTemplate.Builder theSectionedItemTemplateBuilder = new SectionedItemTemplate.Builder();

    private CarIcon playPrevious = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.play_previous)).build();
    private CarIcon playPause = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.play_pause)).build();
    private CarIcon playNext = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.play_next)).build();
    private CarIcon playContinue = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.play)).build();
    private CarIcon exitCarIcon = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.exit)).build();

    private String albumInformation = null;
    private String errorInformation = null;
    private Boolean isPlaying = false;
    private long currentPosition = 0;

    public CarPlayerScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @Override
    public @NonNull Template onGetTemplate() {

        rowSectionBuilder.clearItems();
        gridSectionBuilder.clearItems();

        theSectionedItemTemplateBuilder.clearSections();

        rowSectionBuilder.addItem(new Row.Builder()
                .setTitle(String.format("Folder: %s",properties.getCurrentFolder().getName()))
                .build());

        rowSectionBuilder.addItem(new Row.Builder()
                .setTitle(null == albumInformation ? " " : albumInformation)
                .build());

        if ( ! ( null == errorInformation ) ) {
            rowSectionBuilder.addItem(new Row.Builder()
                    .setTitle(errorInformation)
                    .build());
        }

        sections.add(rowSectionBuilder.build());

        if ( null == errorInformation ) {

            gridSectionBuilder.addItem(new GridItem.Builder()
                    .setTitle(" ")
                    .setImage(playPrevious,GridItem.IMAGE_TYPE_LARGE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            theMusicExplorerInterface.previous();
                        }
                    })
                    .build());

            gridSectionBuilder.addItem(new GridItem.Builder()
                    .setTitle(" ")
                    .setImage(isPlaying ? playPause : playContinue,GridItem.IMAGE_TYPE_LARGE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            if ( isPlaying )
                                theMusicExplorerInterface.pause();
                            else
                                theMusicExplorerInterface.play();
                        }
                    })
                    .build());

            gridSectionBuilder.addItem(new GridItem.Builder()
                    .setTitle(" ")
                    .setImage(playNext,GridItem.IMAGE_TYPE_LARGE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            theMusicExplorerInterface.next();
                        }
                    })
                    .build());

            sections.add(gridSectionBuilder.build());

        }
        /*
        I CANNOT FIGURE OUT THE FUCKING DOCUMENTATION FOR THIS - IT IS SAID TO BE
        AVAILABLE FOR MEDIA APPS AND OTHERS BUT SOMETIMES IT INFERS THAT IT'S MEDIA APP ONLY
        IN ANY CASE IT DOES NOT SHOW

                FUCK YOU GOOGLE !!!!

        progressBarRowSectionBuilder.addItem(new Row.Builder()
                .setTitle(" ")
                .setProgressBar(new CarProgressBar.Builder(1.0f).setColor(CarColor.GREEN).build()).build());

        theSectionedItemTemplateBuilder.addSection(progressBarRowSectionBuilder.build());*/

        Action extraAction = new Action.Builder()
                .setIcon(exitCarIcon)
                .setOnClickListener(() -> {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Util.broadcast(theMusicExplorer,"com.envisionate.musicexplorer.STOP_REQUESTED");
                        }
                    });
                })
                .build();

        Header theHeader = new Header.Builder()
                .setTitle(getCarContext().getString(R.string.currently_playing))
                .setStartHeaderAction(Action.BACK)
                .addEndHeaderAction(extraAction)
                .build();

        theSectionedItemTemplateBuilder.setHeader(theHeader);

        getCarContext().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if ( 1 < getScreenManager().getStackSize() )
                    getScreenManager().pop();
                else {
                    theMusicExplorerInterface.gotoParent(true);
                    currentAutoScreen = new CarEntitiesScreen(getScreenManager().getTop().getCarContext());
                    getScreenManager().push(currentAutoScreen);
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Util.broadcast(theMusicExplorer,"com.envisionate.musicexplorer.NAVIGATION_NOTIFY");
                        }
                    });
                }
                theMusicExplorerInterface.stopPlay();
            }
        });

        theSectionedItemTemplateBuilder.setSections(sections);

        return theSectionedItemTemplateBuilder.build();
    }

    // Player.Listener methods:

    @Override
    public void onMediaMetadataChanged(MediaMetadata md) {
        if ( null == md.albumTitle )
            return;
        String s = md.title.toString();
        if ( -1 < s.lastIndexOf('.') )
            s = s.substring(0,s.lastIndexOf('.'));
        albumInformation = String.format("Track: %s",s);
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    @Override
    public void onEvents(Player player, Player.Events events) {

        if ( events.contains(EVENT_IS_PLAYING_CHANGED) ) {
            isPlaying = player.isPlaying();
            errorInformation = null;
            invalidate();
        }

        if ( events.contains(EVENT_TIMELINE_CHANGED) )
            currentPosition = player.getCurrentPosition();

        if ( events.contains(EVENT_PLAYER_ERROR) ) {
            MediaItem mediaItem = player.getCurrentMediaItem();
            String s = DocumentFile.fromTreeUri(getCarContext(),mediaItem.localConfiguration.uri).getName();
            albumInformation = String.format("Track: %s",s);
            errorInformation = getCarContext().getString(R.string.track_error);
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
        Log.d("PLAY",String.format("position: %d",currentPosition));
    }
}
