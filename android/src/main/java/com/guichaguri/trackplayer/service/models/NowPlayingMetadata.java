package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import android.os.Bundle;

public class NowPlayingMetadata extends TrackMetadata {

    public double elapsedTime;

    public NowPlayingMetadata(Context context, Bundle bundle, int ratingType) {
        setMetadata(context, bundle, ratingType);
    }

    @Override
    public void setMetadata(Context context, Bundle bundle, int ratingType) {
        super.setMetadata(context, bundle, ratingType);

        elapsedTime = bundle.getDouble("elapsedTime", 0);
    }
    
}
