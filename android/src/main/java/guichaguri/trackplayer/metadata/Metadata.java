package guichaguri.trackplayer.metadata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.metadata.components.ArtworkLoader;
import guichaguri.trackplayer.metadata.components.ButtonListener;
import guichaguri.trackplayer.metadata.components.CustomVolume;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.metadata.components.NoisyReceiver;
import guichaguri.trackplayer.player.Playback;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class Metadata {

    private final Context context;
    private final MediaSessionCompat session;
    private final MediaNotification notification;
    private final NoisyReceiver noisyReceiver;

    private ArtworkLoader artwork = null;
    private CustomVolume volume = null;
    private MediaMetadataCompat.Builder md = new MediaMetadataCompat.Builder();
    private PlaybackStateCompat.Builder pb = new PlaybackStateCompat.Builder();

    private long capabilities = 0;
    private int ratingType = RatingCompat.RATING_HEART;
    private int jumpInterval = 15;
    private int maxArtworkSize = 2000;
    private Uri artworkUrl = null;

    public Metadata(Context context, MediaManager manager) {
        this.context = context;

        session = new MediaSessionCompat(context, "TrackPlayer");

        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new ButtonListener(context, manager));

        notification = new MediaNotification(context, session);

        noisyReceiver = new NoisyReceiver(context, session);
    }

    public void setEnabled(boolean enabled) {
        // Change the MediaSession visibility
        session.setActive(enabled);
    }

    public Token getToken() {
        return session.getSessionToken();
    }

    @SuppressWarnings("WrongConstant")
    public void updateOptions(Bundle data) {
        // Update notification options
        notification.updateOptions(data);

        // Load the options
        ratingType = (int)data.getDouble("ratingType", ratingType);
        maxArtworkSize = (int)data.getDouble("maxArtworkSize", maxArtworkSize);
        jumpInterval = (int)data.getDouble("jumpInterval", jumpInterval);

        // Update the rating type
        session.setRatingType(ratingType);

        // Update the capabilities
        List<Integer> array = data.getIntegerArrayList("capabilities");

        if(array != null) {
            capabilities = 0;
            for(int cap : array) {
                capabilities |= cap;
            }
        }
    }

    public void updateMetadata(Playback playback, Track track) {
        // Reset the metadata when there's no playback attached or track playing
        if(playback == null || playback.getCurrentTrack() == null) {
            if(artwork != null) artwork.interrupt();
            md = new MediaMetadataCompat.Builder();
            MediaMetadataCompat metadata = md.build();
            session.setMetadata(metadata);
            notification.updateMetadata(metadata);
            artwork = null;
            return;
        }

        long duration = playback.getDuration();
        if(duration <= 0) duration = track.duration;

        // Fill the metadata builder
        md.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title);
        md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album);
        md.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist);
        md.putString(MediaMetadataCompat.METADATA_KEY_GENRE, track.genre);
        md.putString(MediaMetadataCompat.METADATA_KEY_DATE, track.date);
        md.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, track.description);
        md.putRating(MediaMetadataCompat.METADATA_KEY_RATING, track.rating);
        md.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);

        // Update the queue item
        pb.setActiveQueueItemId(track.queueId);

        // Load the artwork
        loadArtwork(track.artwork, track.artworkLocal);

        // Update the metadata to the MediaSession and the notification
        MediaMetadataCompat metadata = md.build();
        session.setMetadata(metadata);
        notification.updateMetadata(metadata);
    }

    @SuppressWarnings("WrongConstant")
    public void updatePlayback(Playback playback) {
        // Reset the playback state when there's no playback attached
        if(playback == null) {
            pb = new PlaybackStateCompat.Builder();
            PlaybackStateCompat state = pb.build();
            session.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
            session.setPlaybackState(state);
            notification.updatePlayback(state);
            noisyReceiver.setEnabled(false);
            volume = null;
            return;
        }

        int playerState = playback.getState();

        // Update the state, position, speed and buffered position
        pb.setState(playerState, playback.getPosition(), playback.getRate());
        pb.setBufferedPosition(playback.getBufferedPosition());

        // Update the capabilities
        pb.setActions(capabilities);

        if(playback.isRemote()) {
            // Set the volume control to remote
            if(volume == null) {
                volume = new CustomVolume(playback, playback.getVolume(), 100, true);
            } else {
                volume.setVolume(playback.getVolume());
            }
            session.setPlaybackToRemote(volume);
        } else {
            // Set the volume control to local
            session.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
            volume = null;
        }

        // Update the playback state to the MediaSession and the notification
        PlaybackStateCompat state = pb.build();
        session.setPlaybackState(state);
        notification.updatePlayback(state);

        // Update the noisy listener to start receiving when it's playing
        noisyReceiver.setEnabled(Utils.isPlaying(playerState));
    }

    public void updateQueue(Playback playback) {
        List<QueueItem> items = new ArrayList<>();
        for(Track track : playback.getQueue()) {
            items.add(track.toQueueItem());
        }
        session.setQueue(items);
    }

    private void loadArtwork(Uri url, boolean local) {
        if(url == null) {
            // Interrupt the artwork thread if it's running
            if(artwork != null) artwork.interrupt();
            artwork = null;

            // Reset the artwork values
            md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null);
            md.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, null);

            return;
        }

        // Ignore the same artwork to not download it again
        if(url.equals(artworkUrl)) return;

        // Interrupt the artwork thread if it's running
        if(artwork != null) artwork.interrupt();

        // Create another thread to load the new artwork
        artwork = new ArtworkLoader(context, this, url, local, maxArtworkSize);
        artwork.start();
    }

    public void updateArtwork(Uri uri, Bitmap bitmap) {
        artwork = null;

        // Fill artwork values
        artworkUrl = uri;
        md.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uri.toString());
        md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);

        // Update the metadata to the MediaSession and the notification
        MediaMetadataCompat metadata = md.build();
        session.setMetadata(metadata);
        notification.updateMetadata(metadata);
    }

    public void handleIntent(Intent intent) {
        // Handle the media button
        MediaButtonReceiver.handleIntent(session, intent);
    }

    public MediaNotification getNotification() {
        return notification;
    }

    public MediaControllerCompat.TransportControls getControls() {
        return session.getController().getTransportControls();
    }

    public int getRatingType() {
        return ratingType;
    }

    public int getJumpInterval() {
        return jumpInterval;
    }

    public void destroy() {
        // Interrupt the artwork thread if it's running
        if(artwork != null) artwork.interrupt();

        // Release the media session
        session.release();
    }

}
