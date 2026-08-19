package com.envisionate.carservice.screen;

import static com.envisionate.musicinfolders.Globals.ANDROID_AUTO_DELAY;
import static com.envisionate.musicinfolders.Globals.currentAutoEntitiesScreen;
import static com.envisionate.musicinfolders.Globals.currentAutoPlayerScreen;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;
import static com.envisionate.musicinfolders.Globals.theMusicExplorerInterface;

import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.OptIn;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.annotations.ExperimentalCarApi;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.CarText;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridSection;
import androidx.car.app.model.Header;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.RowSection;
import androidx.car.app.model.Section;
import androidx.car.app.model.SectionedItemTemplate;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;
import androidx.documentfile.provider.DocumentFile;

import com.envisionate.musicinfolders.R;
import com.envisionate.musicinfolders.Util;
import com.envisionate.musicinfolders.filesAndFolders;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@ExperimentalCarApi
public class CarEntitiesScreen extends Screen {

    private int columnCount = 0;
    private CarEntitiesScreen thisScreen = null;
    private static CarEntitiesScreen waitingScreen = null;

    private List<GridSection.Builder> theGridSectionBuilders = new ArrayList<>();
    private RowSection.Builder rowSectionBuilder = new RowSection.Builder();
    private RowSection.Builder tracksRowSectionBuilder = new RowSection.Builder();
    private List<Section<?>> sections = new ArrayList<>();

    private CarIcon exitCarIcon = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.exit)).build();

    private SectionedItemTemplate.Builder theSectionedItemTemplateBuilder = new SectionedItemTemplate.Builder();

    TreeMap<String,DocumentFile> nameToUri = new TreeMap<String,DocumentFile>();

    private Boolean navBusy = false;

    public CarEntitiesScreen(CarContext carContext) {
        super(carContext);
    }

    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    public Template onGetTemplate() {

        thisScreen = this;
        waitingScreen = thisScreen;
        navBusy = false;

        if ( null == theMusicExplorer ) {
            Util.doLater(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("com.envisionate.musicinfolders.START");
                    intent.setPackage("com.envisionate.musicinfolders");
                    intent.putExtra("StartedByCar","true");
                    thisScreen.getCarContext().sendBroadcast(intent);
                }
            },ANDROID_AUTO_DELAY);
            CarText theMessage = new CarText.Builder(getCarContext().getString(R.string.starting_on_phone)).build();
            //CarToast.makeText(thisScreen.getCarContext(),"Sarting up MusicExplorer",CarToast.LENGTH_LONG).show();
            return new MessageTemplate.Builder(theMessage).setHeader(new Header.Builder().setTitle(new CarText.Builder("Note").build()).build()).build();
        }

        if ( ! theMusicExplorerInterface.hasRoot() ) {
           Util.doLater(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("com.envisionate.musicinfolders.SETUP_NOTIFY");
                    intent.setPackage("com.envisionate.musicinfolders");
                    thisScreen.getCarContext().sendBroadcast(intent);
                }
            },ANDROID_AUTO_DELAY);
            CarText theMessage = new CarText.Builder(getCarContext().getString(R.string.setting_up_on_phone)).build();
            return new MessageTemplate.Builder(theMessage).setHeader(new Header.Builder().setTitle(new CarText.Builder("Note").build()).build()).build();
        }

        waitingScreen = null;

        columnCount = properties.getAutoDisplayColumns();

        filesAndFolders theEntities = theMusicExplorer.getDisplayedFilesAndFolder();

        if ( null == theEntities ) {
            getCarContext().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if ( navBusy )
                        return;
                    theMusicExplorerInterface.gotoParent();
                    navBusy = false;
                }
            });
            CarText theMessage = new CarText.Builder(getCarContext().getString(R.string.empty_folder)).build();
            return new MessageTemplate.Builder(theMessage).setHeader(new Header.Builder().setStartHeaderAction(Action.BACK).setTitle(new CarText.Builder("Note").build()).build()).build();
        }

        CarIcon trackIcon = new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.track)).build();

        rowSectionBuilder.clearItems();

        if ( ! theMusicExplorerInterface.isRoot() ) {
            rowSectionBuilder.addItem(new Row.Builder()
                    .setTitle(String.format("Folder: %s",properties.getCurrentFolder().getName()))
                    .build());
            sections.add(rowSectionBuilder.build());
        }

        theGridSectionBuilders.add(new GridSection.Builder());

        int itemIndex = 0;
        int builderIndex = 0;
        nameToUri.clear();

        for ( DocumentFile df : theEntities.getFolders() ) {
            itemIndex++;
            nameToUri.put(df.getName(),df);
            theGridSectionBuilders.get(builderIndex).addItem(new GridItem.Builder()
                .setTitle(" ")
                .setImage(carIconFromFolder(df),GridItem.IMAGE_TYPE_LARGE)
                .setOnClickListener( () -> {
                    if ( navBusy )
                        return;
                    navBusy = true;
                    Util.doLater(new Runnable() {
                                     @Override
                                     public void run() {
                                         navBusy = false;
                                     }
                                 },500);
                    theMusicExplorerInterface.onItemClicked(nameToUri.get(df.getName()), null, null, true);
                })
                .build());

            if ( columnCount == itemIndex ) {
                sections.add(theGridSectionBuilders.get(builderIndex).build());
                theGridSectionBuilders.add(new GridSection.Builder());
                itemIndex = 0;
                builderIndex++;
            }

        }

        if ( 0 < itemIndex % columnCount ) {
            sections.add(theGridSectionBuilders.get(builderIndex).build());
            theGridSectionBuilders.add(new GridSection.Builder());
        }

        tracksRowSectionBuilder.clearItems();

        for ( DocumentFile df : theEntities.getFiles() ) {
            nameToUri.put(df.getName(),df);
            String s = df.getName();
            if ( -1 < s.lastIndexOf('.') )
                s = s.substring(0,s.lastIndexOf('.'));
            tracksRowSectionBuilder.addItem(new Row.Builder()
                .setTitle(s)
                .setImage(trackIcon,Row.IMAGE_TYPE_LARGE)
                .setOnClickListener( () -> {
                    CarPlayerScreen carPlayerScreen = new CarPlayerScreen(getCarContext());
                    theMusicExplorerInterface.setCurrentTrack(df);
                    theMusicExplorerInterface.onItemClicked(nameToUri.get(df.getName()),carPlayerScreen, () -> {
                        getScreenManager().push(carPlayerScreen);
                        thisScreen.clearTemplate();
                    },true);
                })
                .build());

        }

        if ( 0 < theEntities.getFiles().size() )
            sections.add(tracksRowSectionBuilder.build());

        Action extraAction = new Action.Builder()
                .setIcon(exitCarIcon)
                .setOnClickListener(() -> {
                        Util.broadcast(theMusicExplorer,"com.envisionate.musicinfolders.STOP_REQUESTED");
                })
                .build();

        if ( ! theMusicExplorerInterface.isRoot() ) {
            Header theHeader = new Header.Builder()
                    .setTitle(String.format("Parent: %s",theMusicExplorer.getParentOf(properties.getCurrentFolder(),false).getName()))
                    .setStartHeaderAction(Action.BACK)
                    .addEndHeaderAction(extraAction)
                    .build();
            theSectionedItemTemplateBuilder.setHeader(theHeader);
            getCarContext().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if ( navBusy )
                        return;
                    navBusy = true;
                    theMusicExplorerInterface.gotoParent();
                }
            });
        } else
            theSectionedItemTemplateBuilder.setHeader(null);

        currentAutoEntitiesScreen = this;
        currentAutoPlayerScreen = null;

        return theSectionedItemTemplateBuilder.setSections(sections).build();
    }


    private CarIcon carIconFromFolder(DocumentFile theFolder) {

        return new CarIcon.Builder(IconCompat.createWithBitmap(theMusicExplorerInterface.getFolderImage(theFolder))).build();
/*
    Bitmap bitmap = Bitmap.createBitmap(FOLDER_WIDTH, FOLDER_HEIGHT, Bitmap.Config.ARGB_8888);

    bitmap.eraseColor(Color.TRANSPARENT);

    Canvas canvas = new Canvas(bitmap);

    Drawable folderBitmap = theMusicExplorer.getResources().getDrawable(R.drawable.folder);

    Rect bounds = new Rect(0,0,FOLDER_WIDTH, FOLDER_HEIGHT);
    folderBitmap.setBounds(bounds);
    folderBitmap.draw(canvas);

    String theText = theFolder.getName();

    TextPaint textPaint = new TextPaint();
    textPaint.setColor(Color.BLACK);
    textPaint.setTextSize(Globals.TEXT_HEIGHT);
    textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

    StaticLayout staticLayout = StaticLayout.Builder.obtain(theText, 0, theText.length(), textPaint, FOLDER_WIDTH - 32)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build();

    RectF textBounds = staticLayout.computeDrawingBoundingBox();

    float textStart = (FOLDER_HEIGHT - textBounds.height())/ 2;
    if ( (textStart + textBounds.height()) > FOLDER_HEIGHT )
        textStart -= (textStart + textBounds.height()) - FOLDER_HEIGHT;

    canvas.save();
    canvas.translate(16, (int)textStart);
    staticLayout.draw(canvas);
    canvas.restore();

    return new CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build();
    */
    }


    public static CarEntitiesScreen getWaitingScreen() {
        return waitingScreen;
    }

    public static void clearWaitingScreen() {
        waitingScreen = null;
    }

    public void clearTemplate() {
        nameToUri.clear();
        sections.clear();
        rowSectionBuilder.clearItems();
        theSectionedItemTemplateBuilder.clearSections();
        for ( GridSection.Builder gsb : theGridSectionBuilders )
            gsb.clearItems();
        theGridSectionBuilders.clear();
    }
}
