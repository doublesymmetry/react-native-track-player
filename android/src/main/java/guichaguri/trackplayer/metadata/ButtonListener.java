package guichaguri.trackplayer.metadata;

import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * @author Guilherme Chaguri
 */
public class ButtonListener extends MediaSessionCompat.Callback {


    @Override
    public void onPlay() {
        super.onPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
    }

    @Override
    public void onSetRating(RatingCompat rating) {
        super.onSetRating(rating);
    }
}
