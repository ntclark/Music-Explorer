package com.envisionate.carservice.screen;

import static android.os.Looper.getMainLooper;
import static androidx.media3.common.Player.EVENT_PLAYER_ERROR;
import static com.envisionate.musicinfolders.Globals.currentAutoEntitiesScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoPlayerScreen;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicExplorerInterface;
import static com.envisionate.musicinfolders.Globals.theMusicPlayer;

import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridSection;
import androidx.car.app.model.Header;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Row;
import androidx.car.app.model.RowSection;
import androidx.car.app.model.Section;
import androidx.car.app.model.SectionedItemTemplate;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.envisionate.musicinfolders.R;
import com.envisionate.musicinfolders.Util;
import com.envisionate.musicinfolders.interfaces.IMusicExplorerPlay;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@ExperimentalCarApi
public class CarPlayerScreen extends Screen implements Player.Listener, IMusicExplorerPlay {

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
    private CarIcon statusCircle = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.status_circle)).build();

    private String albumInformation = null;
    private String trackInformation = null;
    private String errorInformation = null;
    private long trackDuration = 0;
    private long currentTrackMS = 0;
    private Boolean inhibitInvalidate = false;

    public CarPlayerScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @Override
    public @NonNull Template onGetTemplate() {

        inhibitInvalidate = false;

        rowSectionBuilder.clearItems();
        gridSectionBuilder.clearItems();

        theSectionedItemTemplateBuilder.clearSections();

        rowSectionBuilder.addItem(new Row.Builder()
                .setTitle(String.format("Folder: %s",properties.getCurrentFolder().getName()))
                .build());

        rowSectionBuilder.addItem(new Row.Builder()
                .setTitle(null == albumInformation ? " " : albumInformation)
                .build());

        rowSectionBuilder.addItem(new Row.Builder()
                .setTitle(null == trackInformation ? " " : trackInformation)
                .build());

        /*
        I wish to fuck I could get some sort of status bar indicater
        to work!!!

        This particular method causes the head unit display to flash on update

        rowSectionBuilder.addItem(new Row.Builder()
                    .setTitle(" ")
                    .setImage(statusCircle,Row.IMAGE_TYPE_SMALL)
                    .build());
        */

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
                    /*
                    At some point, the following threw a null reference, probably because getPlayer() returned
                    null from theMusicPlayer
                    Wasn't able to repeat it
                    */
                    .setImage(theMusicPlayer.getPlayer().isPlaying() ? playPause : playContinue,GridItem.IMAGE_TYPE_LARGE)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick() {
                            if ( theMusicPlayer.getPlayer().isPlaying() )
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

/*            gridSectionBuilder.addItem(new GridItem.Builder()
                    .setTitle(" ")
                    .setImage(statusCircle,GridItem.IMAGE_TYPE_ICON)
                    .build());*/

            sections.add(gridSectionBuilder.build());

        }

        Action extraAction = new Action.Builder()
                .setIcon(exitCarIcon)
                .setOnClickListener(() -> {
                     Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.STOP_REQUESTED_BY_CAR");
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
                inhibitInvalidate = true;
                theMusicExplorerInterface.gotoParent();
            }
        });

        theSectionedItemTemplateBuilder.setSections(sections);

        currentAutoEntitiesScreen = null;
        currentAutoPlayerScreen = this;

        trackDuration = 0;

        return theSectionedItemTemplateBuilder.build();
    }

    @Override
    public void onPlayTrackMSChanged(long newTrackMS) {
        /*
        I wish to fuck I could get some sort of status bar indicater
        to work!!!

        This particular method causes the head unit display to flash on update

        currentTrackMS = newTrackMS;
        if ( 0 == trackDuration )
            trackDuration = theMusicPlayer.getTrackDuration();
        statusCircle = new CarIcon.Builder(IconCompat.createWithBitmap(Util.getProgressCircle(currentTrackMS,trackDuration))).build();
        invalidate();
        */
    }

    // Player.Listener methods:

    @Override
    public void onIsPlayingChanged(boolean playerIsPlaying) {
        if ( inhibitInvalidate )
            return;
        invalidate();
    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata md) {
        if ( inhibitInvalidate )
            return;
        String newAlbumInformation = String.format("Album: %s",null == md.albumTitle ? "unknown" : md.albumTitle.toString());
        String newTrackInformation = String.format("Track: %s",null == md.title ? "unknown" : md.title.toString());
        if ( ! ( null == albumInformation ) && ! ( null == trackInformation ) )
            if ( newAlbumInformation.equals(albumInformation) && newTrackInformation.equals(trackInformation) )
                return;
        albumInformation = newAlbumInformation;
        trackInformation = newTrackInformation;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    @Override
    public void onEvents(Player player, Player.Events events) {
        if ( inhibitInvalidate )
            return;
        if ( events.contains(EVENT_PLAYER_ERROR) ) {
            MediaItem mediaItem = player.getCurrentMediaItem();
            String s = DocumentFile.fromTreeUri(getCarContext(),mediaItem.localConfiguration.uri).getName();
            trackInformation = String.format("Track: %s",s);
            errorInformation = getCarContext().getString(R.string.track_error);
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

}
