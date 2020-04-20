package com.guichaguri.trackplayer.service.metadata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;
import java.util.List;

/**
 * @author Guichaguri
 */
public class ButtonEvents extends MediaSessionCompat.Callback {

    private final MusicManager manager;

    public ButtonEvents(MusicManager manager) {
        this.manager = manager;
    }

    @Override
    public void onPlay() {
        manager.emitEvent(MusicEvents.BUTTON_PLAY, null);
    }

    @Override
    public void onPause() {
        manager.emitEvent(MusicEvents.BUTTON_PAUSE, null);
    }

    @Override
    public void onStop() {
        manager.emitEvent(MusicEvents.BUTTON_STOP, null);
    }


    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        WritableMap map = Arguments.createMap();
        map.putString("id", mediaId);
        manager.emitEvent(MusicEvents.BUTTON_PLAY_FROM_ID, map);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        WritableMap map = Arguments.createMap();
        map.putString("query", query);

        if(extras.containsKey(MediaStore.EXTRA_MEDIA_FOCUS)) {
            String focus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);

            if(MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "artist";
            } else if(MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "album";
            } else if(MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "playlist";
            } else if(MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "genre";
            } else if(MediaStore.Audio.Media.ENTRY_CONTENT_TYPE.equals(focus)) {
                focus = "title";
            }

            map.putString("focus", focus);
        }

        if(extras.containsKey(MediaStore.EXTRA_MEDIA_TITLE))
            map.putString("title", extras.getString(MediaStore.EXTRA_MEDIA_TITLE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ARTIST))
            map.putString("artist", extras.getString(MediaStore.EXTRA_MEDIA_ARTIST));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ALBUM))
            map.putString("album", extras.getString(MediaStore.EXTRA_MEDIA_ALBUM));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_GENRE))
            map.putString("genre", extras.getString(MediaStore.EXTRA_MEDIA_GENRE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_PLAYLIST))
            map.putString("playlist", extras.getString(MediaStore.EXTRA_MEDIA_PLAYLIST));

        manager.emitEvent(MusicEvents.BUTTON_PLAY_FROM_SEARCH, map);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        List<Track> tracks = manager.getPlayback().getQueue();

        for(Track track : tracks) {
            if(track.queueId != id) continue;

            WritableMap map = Arguments.createMap();
            map.putString("id", track.id);
            manager.emitEvent(MusicEvents.BUTTON_SKIP, map);
            break;
        }
    }

    @Override
    public void onSkipToPrevious() {
        manager.emitEvent(MusicEvents.BUTTON_SKIP_PREVIOUS, null);
    }

    @Override
    public void onSkipToNext() {
        manager.emitEvent(MusicEvents.BUTTON_SKIP_NEXT, null);
    }

    @Override
    public void onRewind() {
        WritableMap map = Arguments.createMap();
        map.putInt("interval", manager.getMetadata().getJumpInterval());
        manager.emitEvent(MusicEvents.BUTTON_JUMP_BACKWARD, map);
    }

    @Override
    public void onFastForward() {
        WritableMap map = Arguments.createMap();
        map.putInt("interval", manager.getMetadata().getJumpInterval());
        manager.emitEvent(MusicEvents.BUTTON_JUMP_FORWARD, map);
    }

    @Override
    public void onSeekTo(long pos) {
        WritableMap map = Arguments.createMap();
        map.putDouble("position", Utils.toSeconds(pos));
        manager.emitEvent(MusicEvents.BUTTON_SEEK_TO, map);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        WritableMap map = Arguments.createMap();
        Utils.setRating(map, "rating", rating);
        manager.emitEvent(MusicEvents.BUTTON_SET_RATING, map);
    }
}
