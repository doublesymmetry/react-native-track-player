package guichaguri.trackplayer.metadata;

import android.support.v4.media.VolumeProviderCompat;

/**
 * @author Guilherme Chaguri
 */
public class CustomVolume extends VolumeProviderCompat {

    public static CustomVolume updateVolume(CustomVolume cv, Boolean canControl, Integer volume, Integer maxVolume) {
        boolean input = false;
        boolean max = false;

        if(volume == null) {
            volume = cv.getCurrentVolume();
        } else {
            cv.setCurrentVolume(volume);
        }

        if(maxVolume == null) {
            maxVolume = cv.getMaxVolume();
        } else {
            max = cv.getMaxVolume() != maxVolume;
        }

        if(canControl == null) {
            canControl = cv.isControllable();
        } else {
            input = cv.isControllable() != canControl;
        }

        if(!input && !max && cv != null) return cv;
        return new CustomVolume(canControl, volume, maxVolume);
    }

    public CustomVolume(boolean canControl, int volume, int maxVolume) {
        super(canControl ? VOLUME_CONTROL_ABSOLUTE : VOLUME_CONTROL_FIXED, maxVolume, volume);
    }

    @Override
    public void onSetVolumeTo(int volume) {
        setCurrentVolume(volume);
    }

    @Override
    public void onAdjustVolume(int direction) {
        int vol = getCurrentVolume();
        int maxVol = getMaxVolume();

        vol += (maxVol / 10) * direction;
        vol = Math.max(vol, 0);
        vol = Math.min(vol, maxVol);

        setCurrentVolume(vol);
    }

    public boolean isControllable() {
        return getVolumeControl() == VOLUME_CONTROL_ABSOLUTE;
    }
}
