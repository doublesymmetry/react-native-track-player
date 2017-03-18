package guichaguri.trackplayer.metadata;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaReceiver;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.metadata.components.ArtworkLoader;
import guichaguri.trackplayer.metadata.components.ButtonListener;
import guichaguri.trackplayer.metadata.components.CustomVolume;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;

/**
 * @author Guilherme Chaguri
 */
public class Metadata {

    private final Context context;
    private final MediaSessionCompat session;
    private final MediaNotification notification;
    private Player player;

    private ArtworkLoader artwork = null;
    private CustomVolume volume = null;
    private MediaMetadataCompat.Builder md = new MediaMetadataCompat.Builder();
    private PlaybackStateCompat.Builder pb = new PlaybackStateCompat.Builder();

    private int ratingType = RatingCompat.RATING_HEART;
    private int maxArtworkSize = 2000;
    private String artworkUri = null;

    public Metadata(Context context) {
        this.context = context;

        ComponentName comp = new ComponentName(context, MediaReceiver.class);
        session = new MediaSessionCompat(context, "TrackPlayer", comp, null);

        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new ButtonListener(context));

        notification = new MediaNotification(context, session);
    }

    public void setEnabled(boolean enabled) {
        // Change the MediaSession and the notification visibility
        session.setActive(enabled);
        notification.setActive(enabled);
    }

    public void attachPlayer(Player player) {
        // Unbind the old player
        if(this.player != null) this.player.bindMetadata(null);

        // Set the player
        this.player = player;

        // Bind the new player
        if(this.player != null) this.player.bindMetadata(this);

        // Update the playback state
        updatePlayback();
    }

    @SuppressWarnings("WrongConstant")
    public void updateOptions(ReadableMap data) {
        // Update notification options
        notification.updateOptions(data);

        // Load the options
        ratingType = Utils.getInt(data, "ratingType", RatingCompat.RATING_HEART);
        maxArtworkSize = Utils.getInt(data, "maxArtworkSize", 2000);

        // Update the rating type
        session.setRatingType(ratingType);
    }

    public void updateMetadata(ReadableMap data) {
        long duration = player != null ? player.getDuration() : 0;

        // Fill the metadata builder
        md.putString(MediaMetadataCompat.METADATA_KEY_TITLE, Utils.getString(data, "title"));
        md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, Utils.getString(data, "album"));
        md.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, Utils.getString(data, "artist"));
        md.putString(MediaMetadataCompat.METADATA_KEY_GENRE, Utils.getString(data, "genre"));
        md.putString(MediaMetadataCompat.METADATA_KEY_DATE, Utils.getString(data, "date"));
        md.putRating(MediaMetadataCompat.METADATA_KEY_RATING, Utils.getRating("rating", data, ratingType));
        md.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Utils.getTime(data, "duration", duration));

        // Load the artwork
        loadArtwork(data);

        // Update the metadata to the MediaSession and the notification
        MediaMetadataCompat metadata = md.build();
        session.setMetadata(metadata);
        notification.updateMetadata(metadata);
    }

    @SuppressWarnings("WrongConstant")
    public void updatePlayback() {
        // Reset the playback state when there's no player attached
        if(player == null) {
            pb = new PlaybackStateCompat.Builder();
            PlaybackStateCompat state = pb.build();
            session.setPlaybackState(state);
            notification.updatePlayback(state);
            return;
        }

        // Update the state, position, speed and buffered position
        pb.setState(player.getState(), player.getPosition(), player.getSpeed(), player.getPositionUpdateTime());
        pb.setBufferedPosition(player.getBufferedPosition());

        if(player instanceof RemotePlayer) {
            // Set the volume control to remote
            RemotePlayer remote = (RemotePlayer)player;
            volume = CustomVolume.updateVolume(remote, volume, 100);
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
    }

    private void loadArtwork(ReadableMap data) {
        if(!data.hasKey("artwork")) {
            // Interrupt the artwork thread if it's running
            if(artwork != null) artwork.interrupt();
            artwork = null;

            // Reset the artwork values
            md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null);
            md.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, null);
        }

        // Get more information about the artwork
        boolean local = ArtworkLoader.isLocal(data, "artwork");
        String uri = ArtworkLoader.getUri(data, "artwork", local);

        // Ignore the same artwork to not download it again
        if(uri == null || uri.equals(artworkUri)) return;

        // Interrupt the artwork thread if it's running
        if(artwork != null) artwork.interrupt();

        // Create another thread to load the new artwork
        artwork = new ArtworkLoader(context, this, local, uri, maxArtworkSize);
        artwork.start();
    }

    public void updateArtwork(String uri, Bitmap bitmap, boolean fromLoader) {
        // Interrupt the artwork thread if it's running
        if(!fromLoader && artwork != null) artwork.interrupt();
        artwork = null;

        // Fill artwork values
        artworkUri = uri;
        md.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uri);
        md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);

        // Update the metadata to the MediaSession and the notification
        MediaMetadataCompat metadata = md.build();
        session.setMetadata(metadata);
        notification.updateMetadata(metadata);
    }

    public void reset() {
        // Interrupt the artwork thread if it's running
        if(artwork != null) artwork.interrupt();

        // Reset properties
        md = new MediaMetadataCompat.Builder();
        pb = new PlaybackStateCompat.Builder();
        volume = null;
        artwork = null;

        // Recreate the metadata and playback state
        MediaMetadataCompat metadata = md.build();
        PlaybackStateCompat state = pb.build();

        // Update the metadata and the state to the MediaSession and the notification
        session.setMetadata(metadata);
        session.setPlaybackState(state);
        notification.updateMetadata(metadata);
        notification.updatePlayback(state);

        // Reset volume control
        session.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
    }

    public void destroy() {
        // Interrupt the artwork thread if it's running
        if(artwork != null) artwork.interrupt();

        // Remove the notification
        notification.setActive(false);

        // Release the media session
        session.release();
    }

}
