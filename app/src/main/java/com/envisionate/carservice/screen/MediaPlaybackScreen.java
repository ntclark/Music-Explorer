package com.envisionate.carservice.screen;

import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.media.model.MediaPlaybackTemplate;
import androidx.car.app.model.Action;
import androidx.car.app.model.Header;
import androidx.car.app.model.Template;

import com.envisionate.musicinfolders.R;

import org.jspecify.annotations.NonNull;

public class MediaPlaybackScreen extends Screen {

    private MediaPlaybackTemplate.Builder theMediaPlaybackTemplateBuilder = new MediaPlaybackTemplate.Builder();

    public MediaPlaybackScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @Override
    public @NonNull Template onGetTemplate() {

        Header theHeader = new Header.Builder()
                .setTitle(getCarContext().getString(R.string.currently_playing))
                .setStartHeaderAction(Action.BACK)
                //.addEndHeaderAction(extraAction)
                .build();

        theMediaPlaybackTemplateBuilder.setHeader(theHeader);

        return theMediaPlaybackTemplateBuilder.build();
    }
}
