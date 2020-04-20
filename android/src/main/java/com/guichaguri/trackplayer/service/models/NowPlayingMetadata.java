package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import com.facebook.react.bridge.ReadableMap;
import com.guichaguri.trackplayer.service.Utils;

public class NowPlayingMetadata extends TrackMetadata {

    public double elapsedTime;

    public NowPlayingMetadata(Context context, ReadableMap data, int ratingType) {
        setMetadata(context, data, ratingType);
    }

    @Override
    public void setMetadata(Context context, ReadableMap data, int ratingType) {
        super.setMetadata(context, data, ratingType);

        elapsedTime = Utils.getDouble(data, "elapsedTime", 0);
    }
    
}
