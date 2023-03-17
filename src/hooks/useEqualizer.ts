import { useEffect, useState } from 'react';
import { Event } from '../constants';
import { EqualizerSettings } from '../interfaces';
import { addEventListener, getEqualizerSettings } from '../trackPlayer';

export const useEqualizer = (): EqualizerSettings | undefined => {
  const [equalizerSettings, setEqualizerSettings] = useState<
    EqualizerSettings | undefined
  >();
  useEffect(() => {
    let mounted = true;

    getEqualizerSettings()
      .then((fetchedEqualizerSettings) => {
        if (!mounted) return;
        // Only set  if it wasn't already set by the listener below:
        setEqualizerSettings((currentEqualizerSettings) =>
          currentEqualizerSettings ? currentEqualizerSettings : fetchedEqualizerSettings
        );
      })
      .catch(() => {
        /** only throws while you haven't yet setup, ignore failure. */
      });

    const sub = addEventListener(Event.EqualizerChanged, (equalizerSettings) => {
      setEqualizerSettings(equalizerSettings);
    });

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return equalizerSettings;
};
