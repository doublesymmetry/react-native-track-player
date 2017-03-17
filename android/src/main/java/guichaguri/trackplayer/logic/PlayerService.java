package guichaguri.trackplayer.logic;

import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends HeadlessJsTaskService {

    public static final String MEDIA_BUTTON = "track_player_media_button";

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {

        if(intent.hasExtra(MEDIA_BUTTON)) {

        } else if(intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            MediaButtonReceiver.handleIntent(null, intent);//TODO
        }

        return new HeadlessJsTaskConfig("track-player", null, 0, true);
    }


}
