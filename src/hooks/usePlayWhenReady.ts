import { useState, useEffect } from 'react';

import { getPlayWhenReady, addEventListener } from '../trackPlayer';
import { Event } from '../constants';

export const usePlayWhenReady = () => {
  const [playWhenReady, setPlayWhenReady] = useState<boolean | undefined>(
    undefined
  );
  useEffect(() => {
    let mounted = true;

    getPlayWhenReady()
      .then((initialState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackPlayWhenReadyChanged listener below:
        setPlayWhenReady((state) => state ?? initialState);
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = addEventListener(
      Event.PlaybackPlayWhenReadyChanged,
      (event) => {
        setPlayWhenReady(event.playWhenReady);
      }
    );

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return playWhenReady;
};
