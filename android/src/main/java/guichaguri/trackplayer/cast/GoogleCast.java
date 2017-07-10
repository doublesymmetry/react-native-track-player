package guichaguri.trackplayer.cast;

import android.content.Context;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import guichaguri.trackplayer.logic.Events;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.player.players.CastPlayback;

/**
 * @author Guilherme Chaguri
 */
public class GoogleCast implements CastStateListener, SessionManagerListener<CastSession> {

    private final Context context;
    private final MediaManager manager;
    private final CastContext cast;

    public GoogleCast(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
        this.cast = CastContext.getSharedInstance(context);

        cast.addCastStateListener(this);
        cast.getSessionManager().addSessionManagerListener(this, CastSession.class);
    }

    public boolean isCurrentSession(CastSession session) {
        return cast.getSessionManager().getCurrentCastSession() == session;
    }

    public void disconnect(boolean stopReceiver) {
        cast.getSessionManager().endCurrentSession(stopReceiver);
    }

    @Override
    public void onCastStateChanged(int state) {
        WritableMap map = Arguments.createMap();
        map.putInt("state", state);
        Events.dispatchEvent(context, Events.CAST_STATE, map);
    }

    @Override
    public void onSessionStarting(CastSession session) {
        Events.dispatchEvent(context, Events.CAST_CONNECTING, null);
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        manager.switchPlayback(new CastPlayback(context, manager, this, session));
        Events.dispatchEvent(context, Events.CAST_CONNECTED, null);
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
        Events.dispatchEvent(context, Events.CAST_CONNECTION_FAILED, null);
    }

    @Override
    public void onSessionEnding(CastSession session) {
        // Last chance to update the position
        manager.getPlayback().updateData();

        Events.dispatchEvent(context, Events.CAST_DISCONNECTING, null);
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
        manager.switchPlayback(manager.createLocalPlayback());
        Events.dispatchEvent(context, Events.CAST_DISCONNECTED, null);
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {

    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {

    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {

    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {

    }
}
