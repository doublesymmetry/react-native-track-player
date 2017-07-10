package guichaguri.trackplayer.metadata.components;

import android.support.v4.media.VolumeProviderCompat;
import guichaguri.trackplayer.player.Playback;

/**
 * @author Guilherme Chaguri
 */
public class CustomVolume extends VolumeProviderCompat {

    private final Playback playback;

    public CustomVolume(Playback playback, float volume, int maxVolume, boolean canControl) {
        super(canControl ? VOLUME_CONTROL_ABSOLUTE : VOLUME_CONTROL_FIXED, maxVolume, (int)(volume * maxVolume));
        this.playback = playback;
    }

    public void setVolume(float volume) {
        setCurrentVolume((int)(volume * getMaxVolume()));
    }

    @Override
    public void onSetVolumeTo(int volume) {
        // Update the volume from both sides
        playback.setVolume(volume / (float)getMaxVolume());
        setCurrentVolume(volume);
    }

    @Override
    public void onAdjustVolume(int direction) {
        int vol = getCurrentVolume();
        int maxVol = getMaxVolume();

        // Increase/decrease the new volume based on the direction by 10%
        vol += (maxVol / 10) * direction;

        // Clamp the volume (0-maxVol)
        vol = Math.max(vol, 0);
        vol = Math.min(vol, maxVol);

        // Update the volume from both sides
        playback.setVolume(vol / (float)maxVol);
        setCurrentVolume(vol);
    }
}
