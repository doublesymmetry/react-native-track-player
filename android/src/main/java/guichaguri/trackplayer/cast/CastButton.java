package guichaguri.trackplayer.cast;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.v7.app.MediaRouteButton;
import android.util.Log;
import android.widget.Toast;
import guichaguri.trackplayer.logic.Utils;

/**
 * A copy of {@link MediaRouteButton}, but can change its color
 *
 * @author Guilherme Chaguri
 */
public class CastButton extends MediaRouteButton {

    private Drawable remoteIndicator;
    private int color = Color.TRANSPARENT;

    public CastButton(Context context) {
        super(context);
    }

    private void updateColor() {
        if(remoteIndicator != null) {
            if(color != Color.TRANSPARENT) {
                remoteIndicator.setColorFilter(color, Mode.SRC_IN);
            } else {
                remoteIndicator.setColorFilter(null);
            }
        }
    }

    public void setColor(int color) {
        this.color = color;
        updateColor();
    }

    @Override
    public void setRemoteIndicatorDrawable(Drawable d) {
        this.remoteIndicator = d;
        updateColor();
        super.setRemoteIndicatorDrawable(d);
    }

    public boolean showDialog() {
        try {
            return super.showDialog();
        } catch(IllegalStateException ex) {
            // This exception may or may not be caused by the activity not extending FragmentActivity
            String msg = "Your activity needs to extend 'ReactFragmentActivity' instead of 'ReactActivity'";
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            Log.w(Utils.TAG, msg);
            ex.printStackTrace();
            return false;
        }
    }
}
