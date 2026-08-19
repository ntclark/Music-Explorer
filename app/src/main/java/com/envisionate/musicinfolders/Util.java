package com.envisionate.musicinfolders;

import static android.os.Looper.getMainLooper;
import static com.envisionate.musicinfolders.Globals.ANDROID_AUTO_ICON_SIDE;
import static com.envisionate.musicinfolders.Globals.properties;
import static com.envisionate.musicinfolders.Globals.theMusicExplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Comparator;

public class Util {

    public Util() {}

    public filesAndFolders getCurrentFilesAndFolders(DocumentFile overrideCurrent) {

        DocumentFile theRoot = null;

        if ( ! ( null == overrideCurrent ) )
            properties.setCurrentFolder(overrideCurrent);

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

        filesAndFolders theFilesAndFolders = new filesAndFolders(new ArrayList<DocumentFile>(),new ArrayList<DocumentFile>());

        if ( 0 == entityArray.length )
            return theFilesAndFolders;

        for ( DocumentFile df : entityArray ) {
            String fn = theMusicExplorer.folderName(df.getName());
            if ( fn.startsWith(".") )
                continue;
            if ( df.isDirectory() )
                theFilesAndFolders.getFolders().add(df);
            else
                theFilesAndFolders.getFiles().add(df);
        }

        theFilesAndFolders.getFolders().sort(new Comparator<DocumentFile>() {
            @Override
            public int compare(DocumentFile o1, DocumentFile o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return theFilesAndFolders;
    }

    public static Bitmap getProgressCircle(long currentPoint,long endPoint) {

        Bitmap bitmap = Bitmap.createBitmap(ANDROID_AUTO_ICON_SIDE,ANDROID_AUTO_ICON_SIDE, Bitmap.Config.ARGB_8888);

        bitmap.eraseColor(android.graphics.Color.TRANSPARENT);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);

        RectF ovalBounds = new RectF(0f, 0f, ANDROID_AUTO_ICON_SIDE, ANDROID_AUTO_ICON_SIDE);

        float startAngle = 0.0f;
        float sweepAngle = 360.0f * (currentPoint - endPoint) / endPoint;
        boolean useCenter = false;

        canvas.drawArc(ovalBounds, startAngle, sweepAngle, useCenter, paint);

        return bitmap;
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
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(theMessage);
                intent.setPackage("com.envisionate.musicinfolders");
                theActivity.getApplicationContext().sendBroadcast(intent);
            }
        });
    }

    public static void broadcastLater(Activity theActivity, String theMessage,long delayMS) {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(theMessage);
                intent.setPackage("com.envisionate.musicinfolders");
                theActivity.getApplicationContext().sendBroadcast(intent);
            }
        },delayMS);
    }

    public static void broadcastFast(Activity theActivity, String theMessage) {
        Intent intent = new Intent(theMessage);
        intent.setPackage("com.envisionate.musicinfolders");
        theActivity.getApplicationContext().sendBroadcast(intent);
    }

    public static void doLater(Runnable theRunnable,long delayMS) {
        new Handler(getMainLooper()).postDelayed(theRunnable,delayMS);
    }

}
