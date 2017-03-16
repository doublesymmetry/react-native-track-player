package guichaguri.trackplayer.logic;

import android.content.Intent;
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

        }

        return new HeadlessJsTaskConfig("track-player", null, 0, true);
    }


}
