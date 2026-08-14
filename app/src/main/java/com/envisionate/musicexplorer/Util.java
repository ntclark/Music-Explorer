package com.envisionate.musicexplorer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;

public final class Util {

    private Util() {}

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
