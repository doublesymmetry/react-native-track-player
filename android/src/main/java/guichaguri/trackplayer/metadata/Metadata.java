package guichaguri.trackplayer.metadata;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.MediaReceiver;
import guichaguri.trackplayer.metadata.components.ArtworkLoader;
import guichaguri.trackplayer.metadata.components.ButtonListener;
import guichaguri.trackplayer.metadata.components.CustomVolume;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.metadata.components.NoisyReceiver;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.RemotePlayer;

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
    private int maxArtworkSize = 2000;
    private String artworkUri = null;

    public Metadata(Context context, MediaManager manager) {
        this.context = context;

        ComponentName comp = new ComponentName(context, MediaReceiver.class);
        session = new MediaSessionCompat(context, "TrackPlayer", comp, null);

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

    @SuppressWarnings("WrongConstant")
    public void updateOptions(ReadableMap data) {
        // Update notification options
        notification.updateOptions(data);

        // Load the options
        ratingType = Utils.getInt(data, "ratingType", RatingCompat.RATING_HEART);
        maxArtworkSize = Utils.getInt(data, "maxArtworkSize", 2000);

        // Update the rating type
        session.setRatingType(ratingType);

        // Update the capabilities
        updateCapabilities(data);
    }

    public void updateMetadata(Player player, ReadableMap data) {
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
    public void updatePlayback(Player player) {
        // Reset the playback state when there's no player attached
        if(player == null) {
            pb = new PlaybackStateCompat.Builder();
            PlaybackStateCompat state = pb.build();
            session.setPlaybackState(state);
            notification.updatePlayback(state);
            noisyReceiver.setEnabled(false);
            return;
        }

        int playerState = player.getState();

        // Update the state, position, speed and buffered position
        pb.setState(playerState, player.getPosition(), player.getSpeed(), player.getPositionUpdateTime());
        pb.setBufferedPosition(player.getBufferedPosition());

        // Update the capabilities
        pb.setActions(capabilities);

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

        // Update the noisy listener to start receiving when it's playing
        noisyReceiver.setEnabled(Utils.isPlaying(playerState));
    }

    private void updateCapabilities(ReadableMap data) {
        capabilities = 0;

        ReadableArray array = Utils.getArray(data, "capabilities", null);
        if(array == null) return;

        for(int i = 0; i < array.size(); i++) {
            if(array.getType(i) == ReadableType.Number) {
                capabilities |= array.getInt(i);
            }
        }
    }

    private void loadArtwork(ReadableMap data) {
        if(!data.hasKey("artwork")) {
            // Interrupt the artwork thread if it's running
            if(artwork != null) artwork.interrupt();
            artwork = null;

            // Reset the artwork values
            md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null);
            md.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, null);

            return;
        }

        // Get more information about the artwork
        boolean local = Utils.isUrlLocal(data, "artwork");
        String uri = Utils.getUrl(data, "artwork", local);

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

    public void handleIntent(Intent intent) {
        MediaButtonReceiver.handleIntent(session, intent);
    }

    public MediaNotification getNotification() {
        return notification;
    }

    public MediaControllerCompat.TransportControls getControls() {
        return session.getController().getTransportControls();
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

        // Release the media session
        session.release();
    }

}
