package guichaguri.trackplayer.cast;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.app.MediaRouteChooserDialogFragment;
import android.support.v7.app.MediaRouteControllerDialogFragment;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import guichaguri.trackplayer.logic.Utils;

/**
 * A copy of {@link MediaRouteButton}, but works without a {@link android.support.v4.app.FragmentActivity}
 *
 * @author Guilherme Chaguri
 */
public class CastButton extends MediaRouteButton {

    private final String CHOOSER_FRAGMENT_TAG = "trackplayer.cast.chooser";
    private final String CONTROLLER_FRAGMENT_TAG = "trackplayer.cast.controller";

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
            // Either way, we'll try using the native FragmentManager instead
            return showNativeDialog();
        }
    }

    private void showDialogFragment(DialogFragment fragment, FragmentManager manager, String tag) {
        SupportFragmentWrapper wrapper = new SupportFragmentWrapper();
        wrapper.setSupportFragment(fragment);
        wrapper.show(manager, tag);
    }

    /**
     * Copy of {@link MediaRouteButton#showDialog()}, but uses the native {@link FragmentManager} instead of the support one
     * @return Whether the dialog was successfully shown
     */
    private boolean showNativeDialog() {
        Activity activity = getActivity();
        FragmentManager fm = activity.getFragmentManager();
        MediaRouteSelector selector = getRouteSelector();
        MediaRouter.RouteInfo route = MediaRouter.getInstance(getContext()).getSelectedRoute();

        if(route.isDefault() || !route.matchesSelector(selector)) {
            if(fm.findFragmentByTag(CHOOSER_FRAGMENT_TAG) != null) {
                Log.w(Utils.TAG, "showDialog(): Route chooser dialog already showing!");
                return false;
            }
            MediaRouteChooserDialogFragment f = getDialogFactory().onCreateChooserDialogFragment();
            f.setRouteSelector(selector);
            showDialogFragment(f, fm, CHOOSER_FRAGMENT_TAG);
        } else {
            if(fm.findFragmentByTag(CONTROLLER_FRAGMENT_TAG) != null) {
                Log.w(Utils.TAG, "showDialog(): Route controller dialog already showing!");
                return false;
            }
            MediaRouteControllerDialogFragment f = getDialogFactory().onCreateControllerDialogFragment();
            showDialogFragment(f, fm, CONTROLLER_FRAGMENT_TAG);
        }

        return true;
    }

    /**
     * Copy of {@link MediaRouteButton#getActivity()} because it's private
     * @return The main activity
     */
    private Activity getActivity() {
        // Gross way of unwrapping the Activity so we can get the FragmentManager
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        throw new IllegalStateException("The MediaRouteButton's Context is not an Activity.");
    }

    /**
     * A wrapper for the {@link DialogFragment} from the support library
     */
    public static class SupportFragmentWrapper extends android.app.DialogFragment {

        private DialogFragment fragment = null;

        public void setSupportFragment(DialogFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if(fragment == null) return super.onCreateDialog(savedInstanceState);
            return fragment.onCreateDialog(savedInstanceState);
        }

        @Override
        public void onStop() {
            super.onStop();
            if(fragment != null) fragment.onStop();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if(fragment != null) fragment.onConfigurationChanged(newConfig);
        }

    }

}
