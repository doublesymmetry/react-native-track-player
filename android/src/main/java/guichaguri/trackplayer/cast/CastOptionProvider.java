package guichaguri.trackplayer.cast;

import android.content.Context;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import guichaguri.trackplayer.BuildConfig;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class CastOptionProvider implements OptionsProvider {
    @Override
    public CastOptions getCastOptions(Context context) {
        String id = BuildConfig.CAST_APPLICATION_ID;
        if(id == null) {
            id = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
        }

        return new CastOptions.Builder()
                .setCastMediaOptions(new CastMediaOptions.Builder()
                    .setNotificationOptions(null)
                    .build())
                .setReceiverApplicationId(id)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
