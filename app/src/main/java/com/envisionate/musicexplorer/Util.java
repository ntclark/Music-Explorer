package com.envisionate.musicexplorer;

import static com.envisionate.musicexplorer.Globals.properties;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;

import com.envisionate.musicexplorer.interfaces.IMusicExplorer;

import java.util.ArrayList;
import java.util.Comparator;

public class Util {

    private Util() {}

    public static class filesAndFolders {

        private static final filesAndFolders instance = new filesAndFolders();

        private static ArrayList<DocumentFile> theFolders;
        private static ArrayList<DocumentFile> theFiles;

        public ArrayList<DocumentFile> getFolders() {
            return theFolders;
        }

        public ArrayList<DocumentFile> getFiles() {
            return theFiles;
        }

    }

    public static filesAndFolders getCurrentFilesAndFolders(DocumentFile overrideCurrent) {

        DocumentFile theRoot = null;

        if ( ! ( null == overrideCurrent ) )
            properties.setCurrentFolder(overrideCurrent);

        filesAndFolders.theFolders = null;
        filesAndFolders.theFiles = null;

        if ( null == properties.getCurrentFolder() ) {
            try {
                theRoot = DocumentFile.fromTreeUri(theMusicExplorer, Uri.parse(properties.getRootFolder()));
            } catch ( Exception ex ) {
                return null;
            }
        }

        DocumentFile [] entityArray = null;

        if ( null == properties.getCurrentFolder() )
            entityArray = theRoot.listFiles();
        else
            entityArray = properties.getCurrentFolder().listFiles();

        if ( 0 == entityArray.length ) {
            if ( ! ( null == properties.getCurrentFolder() ) )
                return null;
            properties.resetPreferences();
            theMusicExplorer.startActivity(new Intent(theMusicExplorer, Settings.class));
            return null;
        }

        filesAndFolders.theFolders = new ArrayList<DocumentFile>();
        filesAndFolders.theFiles = new ArrayList<DocumentFile>();

        for ( DocumentFile df : entityArray ) {
            String fn = theMusicExplorer.folderName(df.getName());
            if ( fn.startsWith(".") )
                continue;
            if ( df.isDirectory() )
                filesAndFolders.theFolders.add(df);
            else
                filesAndFolders.theFiles.add(df);
        }

        filesAndFolders.theFolders.sort(new Comparator<DocumentFile>() {
            @Override
            public int compare(DocumentFile o1, DocumentFile o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return filesAndFolders.instance;
    }

    public static Bitmap getFolderImage(AppCompatActivity theActivity, DocumentFile theFolder) {

        ConstraintLayout layout = new ConstraintLayout(theActivity);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(224,224);

        layout.setId(R.id.folder_layout);
        layout.setLayoutParams(layoutParams);

        ImageView imageView = null;

        if ( 1 == 0 ) {
            imageView = new ImageView(theActivity);
            imageView.setId(R.id.folder_image_view);
            ConstraintLayout.LayoutParams imageViewParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setImageResource(R.drawable.folder);
            imageViewParams.bottomToBottom = R.id.folder_layout;
            imageViewParams.leftToLeft = R.id.folder_layout;
            imageViewParams.topToTop = R.id.folder_layout;
            imageViewParams.verticalBias = 0.0f;
            imageView.setLayoutParams(imageViewParams);
        }

        float textSize = 0.0f;

        TextView textView = new TextView(theActivity);

        textView.setId(R.id.folder_text_view);

        ConstraintLayout.LayoutParams textViewParams = new ConstraintLayout.LayoutParams(0,224);

        textViewParams.width = 224;
        textViewParams.height = 224;

        if ( ! ( null == imageView ) ) {
            textViewParams.leftMargin = 16;
            textViewParams.rightMargin = 16;
        }

        textViewParams.bottomToBottom = R.id.folder_layout;
        textViewParams.endToEnd = R.id.folder_layout;
        textViewParams.horizontalBias = 0;
        textViewParams.startToStart = R.id.folder_layout;
        textViewParams.topToTop = R.id.folder_layout;
        textViewParams.verticalBias = 1.7f;

        textView.setLayoutParams(textViewParams);
        textView.setForegroundGravity(Gravity.CENTER);

        if ( null == imageView ) {
            textView.setTextColor(Color.WHITE);
            textSize = 28.0f;
            textView.setTextSize(textSize);
        } else {
            textView.setTextColor(Color.BLACK);
            textSize = 20.0f;
            textView.setTextSize(textSize);
        }

        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

        String theText = theFolder.getName();

        textView.setText(theText);

        if ( ! ( null == imageView ) )
            layout.addView(imageView);

        layout.addView(textView);

        layout.measure(224,224);
        layout.layout(0, 0, 224,224);
        int textHeight = textView.getLineCount() * textView.getLineHeight();

        while ( 224 < textHeight && 10.0f < textSize ) {
            textSize = textSize - 1.0f;
            textView.setTextSize(textSize);
            layout.invalidate();
            layout.measure(layout.getWidth(),layout.getHeight());
            layout.layout(0, 0, 224,224);
            textHeight = textView.getLineCount() * textView.getLineHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(layout.getMeasuredWidth(), layout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        layout.draw(canvas);

        return bitmap;
    }

    public static void broadcast(Activity theActivity, String theMessage) {
        Intent intent = new Intent(theMessage);
        intent.setPackage("com.envisionate.musicexplorer");
        theActivity.getApplicationContext().sendBroadcast(intent);
    }
}
