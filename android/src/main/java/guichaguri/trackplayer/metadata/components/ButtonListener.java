package guichaguri.trackplayer.metadata.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import guichaguri.trackplayer.logic.Events;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;

/**
 * @author Guilherme Chaguri
 */
public class ButtonListener extends MediaSessionCompat.Callback {

    private final Context context;
    private final MediaManager manager;

    public ButtonListener(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
    }

    @Override
    public void onPlay() {
        Events.dispatchEvent(context, Events.BUTTON_PLAY, null);
    }

    @Override
    public void onPause() {
        Events.dispatchEvent(context, Events.BUTTON_PAUSE, null);
    }

    @Override
    public void onStop() {
        Events.dispatchEvent(context, Events.BUTTON_STOP, null);
    }

    @Override
    public void onSkipToNext() {
        Events.dispatchEvent(context, Events.BUTTON_SKIP_NEXT, null);
    }

    @Override
    public void onSkipToPrevious() {
        Events.dispatchEvent(context, Events.BUTTON_SKIP_PREVIOUS, null);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        for(Track track : manager.getPlayback().getQueue()) {
            if(track.queueId == id) {
                Bundle bundle = new Bundle();
                bundle.putString("id", track.id);
                Events.dispatchEvent(context, Events.BUTTON_SKIP, bundle);
                break;
            }
        }
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        // Required for Android Auto
        Bundle bundle = new Bundle();
        bundle.putString("id", mediaId);
        Events.dispatchEvent(context, Events.BUTTON_PLAY_FROM_ID, bundle);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        // Required for Android Auto
        Bundle bundle = new Bundle();
        bundle.putString("query", query);

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
            bundle.putString("focus", focus);
        }

        if(extras.containsKey(MediaStore.EXTRA_MEDIA_TITLE))
            bundle.putString("title", extras.getString(MediaStore.EXTRA_MEDIA_TITLE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ARTIST))
            bundle.putString("artist", extras.getString(MediaStore.EXTRA_MEDIA_ARTIST));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_ALBUM))
            bundle.putString("album", extras.getString(MediaStore.EXTRA_MEDIA_ALBUM));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_GENRE))
            bundle.putString("genre", extras.getString(MediaStore.EXTRA_MEDIA_GENRE));
        if(extras.containsKey(MediaStore.EXTRA_MEDIA_PLAYLIST))
            bundle.putString("playlist", extras.getString(MediaStore.EXTRA_MEDIA_PLAYLIST));

        Events.dispatchEvent(context, Events.BUTTON_PLAY_FROM_SEARCH, bundle);
    }

    @Override
    public void onSeekTo(long pos) {
        Bundle bundle = new Bundle();
        Utils.setTime(bundle, "position", pos);
        Events.dispatchEvent(context, Events.BUTTON_SEEK_TO, bundle);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        Bundle bundle = new Bundle();
        Utils.setRating(bundle, "rating", rating);
        Events.dispatchEvent(context, Events.BUTTON_SET_RATING, bundle);
    }

    @Override
    public void onFastForward() {
        Bundle bundle = new Bundle();
        bundle.putInt("interval", manager.getMetadata().getJumpInterval());
        Events.dispatchEvent(context, Events.BUTTON_JUMP_FORWARD, bundle);
    }

    @Override
    public void onRewind() {
        Bundle bundle = new Bundle();
        bundle.putInt("interval", manager.getMetadata().getJumpInterval());
        Events.dispatchEvent(context, Events.BUTTON_JUMP_BACKWARD, bundle);
    }
}
