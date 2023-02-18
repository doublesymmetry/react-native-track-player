import { useEffect, useState } from 'react';

import { getPlaybackState, addEventListener } from '../trackPlayer';
import { Event } from '../constants';
import type { PlaybackState } from '../interfaces';

/**
 * Get current playback state and subsequent updates.
 *
 * Note: While it is fetching the initial state from the native module, the
 * returned state property will be `undefined`.
 * */
export const usePlaybackState = (): PlaybackState | { state: undefined } => {
  const [playbackState, setPlaybackState] = useState<
    PlaybackState | { state: undefined }
  >({
    state: undefined,
  });
  useEffect(() => {
    let mounted = true;

    getPlaybackState()
      .then((fetchedState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackState listener below:
        setPlaybackState((currentState) =>
          currentState.state ? currentState : fetchedState
        );
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = addEventListener(Event.PlaybackState, (state) => {
      setPlaybackState(state);
    });

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return playbackState;
};
