package guichaguri.trackplayer.metadata.components;

import android.support.v4.media.VolumeProviderCompat;
import guichaguri.trackplayer.player.RemotePlayer;

/**
 * @author Guilherme Chaguri
 */
public class CustomVolume extends VolumeProviderCompat {

    public static CustomVolume updateVolume(RemotePlayer player, CustomVolume cv, int maxVolume) {
        // Read information from the player
        boolean canControl = player.canChangeVolume();
        int volume = (int)(player.getVolume() * maxVolume);

        if(cv != null && cv.isControllable() == canControl && cv.getMaxVolume() == maxVolume) {
            // No need to recreate the provider, we will use the old one
            cv.setCurrentVolume(volume);
            return cv;
        }

        // Create a new volume provider
        return new CustomVolume(player, volume, maxVolume, canControl);
    }

    private final RemotePlayer player;

    private CustomVolume(RemotePlayer player, int volume, int maxVolume, boolean canControl) {
        super(canControl ? VOLUME_CONTROL_ABSOLUTE : VOLUME_CONTROL_FIXED, maxVolume, volume);
        this.player = player;
    }

    @Override
    public void onSetVolumeTo(int volume) {
        // Update the volume from both sides
        player.setVolume(volume / (float)getMaxVolume());
        setCurrentVolume(volume);
    }

    @Override
    public void onAdjustVolume(int direction) {
        int vol = getCurrentVolume();
        int maxVol = getMaxVolume();

        // Increase/decrease the new volume based on the direction by 10%
        vol += (maxVol / 10) * direction;

        // Clamp the volume
        vol = Math.max(vol, 0);
        vol = Math.min(vol, maxVol);

        // Update the volume from both sides
        player.setVolume(vol / (float)maxVol);
        setCurrentVolume(vol);
    }

    private boolean isControllable() {
        return getVolumeControl() != VOLUME_CONTROL_FIXED;
    }
}
