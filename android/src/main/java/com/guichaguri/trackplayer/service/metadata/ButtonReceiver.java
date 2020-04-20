package com.guichaguri.trackplayer.service.metadata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.media.session.MediaButtonReceiver;

public class ButtonReceiver extends BroadcastReceiver {

    private final MetadataManager manager;

    public ButtonReceiver(MetadataManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaButtonReceiver.handleIntent(manager.getSession(), intent);
    }

}
