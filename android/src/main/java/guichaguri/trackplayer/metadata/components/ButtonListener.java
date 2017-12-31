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
import guichaguri.trackplayer.player.Playback;

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
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.play();
    }

    @Override
    public void onPause() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.pause();
    }

    @Override
    public void onStop() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.stop();
    }

    @Override
    public void onSkipToNext() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.skipToNext(null);
    }

    @Override
    public void onSkipToPrevious() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.skipToPrevious(null);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        for(Track track : manager.getPlayback().getQueue()) {
            if(track.queueId == id) {
                pb.skip(track.id, null);
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
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        pb.seekTo(pos);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        Bundle bundle = new Bundle();
        Utils.setRating(bundle, "rating", rating);
        Events.dispatchEvent(context, Events.BUTTON_SET_RATING, bundle);
    }

    @Override
    public void onFastForward() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        long pos = pb.getPosition() + manager.getMetadata().getJumpInterval();
        if(pos > pb.getDuration()) pos = pb.getDuration();

        pb.seekTo(pos);
    }

    @Override
    public void onRewind() {
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        long pos = pb.getPosition() - manager.getMetadata().getJumpInterval();
        if(pos < 0) pos = 0;

        pb.seekTo(pos);
    }
}
