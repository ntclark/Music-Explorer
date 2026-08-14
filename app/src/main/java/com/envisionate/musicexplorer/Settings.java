package com.envisionate.musicexplorer;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.envisionate.musicexplorer.Globals.theMusicExplorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.car.app.annotations.ExperimentalCarApi;

import com.envisionate.carservice.screen.CarEntitiesScreen;

public class Settings extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String s = Uri.parse(theMusicExplorer.properties.getRootFolder()).getPath();
        if ( -1 < s.lastIndexOf((':') ) )
            s = s.substring(s.lastIndexOf(':') + 1);

        ((TextView)findViewById(R.id.root_folder)).setText(s);

        ((Button)findViewById(R.id.root_folder_get)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, theMusicExplorer.properties.getRootFolder());
                startActivityForResult(intent, 9999);
            }
        });

        ((TextView)findViewById(R.id.auto_display_columns_value)).setText(String.format("%d",theMusicExplorer.properties.getAutoDisplayColumns()));

        ((Button)findViewById(R.id.auto_display_columns_value_plus)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int val = theMusicExplorer.properties.getAutoDisplayColumns() + 1;
                theMusicExplorer.properties.setAutoDisplayColumns(val);
                ((TextView)findViewById(R.id.auto_display_columns_value)).setText(String.format("%d",theMusicExplorer.properties.getAutoDisplayColumns()));
            }
        });

        ((Button)findViewById(R.id.auto_display_columns_value_minus)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int val = Math.max(1,theMusicExplorer.properties.getAutoDisplayColumns() - 1);
                theMusicExplorer.properties.setAutoDisplayColumns(val);
                ((TextView)findViewById(R.id.auto_display_columns_value)).setText(String.format("%d",theMusicExplorer.properties.getAutoDisplayColumns()));
            }
        });

        ((Switch)findViewById(R.id.resume_play_switch)).setChecked(theMusicExplorer.properties.getResumePlayOnStart());

        ((Switch)findViewById(R.id.resume_play_switch)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                theMusicExplorer.properties.setResumePlayOnStart(((Switch)v).isChecked());
            }
        });
/*

        ((Switch)findViewById(R.id.auto_display_folder_icons_switch)).setChecked(theMusicExplorer.properties.getAutoDisplayFolderIcons());

        ((Switch)findViewById(R.id.auto_display_folder_icons_switch)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Boolean val = theMusicExplorer.properties.getAutoDisplayFolderIcons();
                theMusicExplorer.properties.setAutoDisplayFolderIcons(! val);
            }
        });
*/

        ((TextView)findViewById(R.id.settings_set_root_warning)).setVisibility(INVISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch ( requestCode ) {
            case 9999:
                Uri uri = data.getData();
                theMusicExplorer.properties.setRootFolder(uri);
                getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                String s = Uri.parse(theMusicExplorer.properties.getRootFolder()).getPath();
                if ( -1 < s.lastIndexOf((':') ) )
                    s = s.substring(s.lastIndexOf(':') + 1);
                ((TextView)findViewById(R.id.root_folder)).setText(s);
                ((TextView)findViewById(R.id.settings_set_root_warning)).setVisibility(INVISIBLE);
                break;
        }
    }


    @OptIn(markerClass = ExperimentalCarApi.class)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == android.R.id.home ) {
            if ( null == theMusicExplorer.properties.getRootFolder() || "<click Browse>".equals(theMusicExplorer.properties.getRootFolder()) ) {
                ((TextView)findViewById(R.id.settings_set_root_warning)).setVisibility(VISIBLE);
                return false;
            }
            if ( ! ( null == CarEntitiesScreen.getWaitingScreen() ) ) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("com.envisionate.musicexplorer.SETUP_DONE");
                        intent.setPackage("com.envisionate.musicexplorer");
                        getApplicationContext().sendBroadcast(intent);
                    }}
                );
            }
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
