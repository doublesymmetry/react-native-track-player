import { useEffect, useRef, useState } from 'react';
import { AppState, AppStateStatus } from 'react-native';

import {
  Event,
  EventsPayloadByEvent,
  PlaybackState,
  Progress,
  Track
} from './interfaces';
import TrackPlayer from './trackPlayer';

export const usePlayWhenReady = () => {
  const [playWhenReady, setPlayWhenReady] = useState<boolean | undefined>(
    undefined
  );
  useEffect(() => {
    let mounted = true;

    TrackPlayer.getPlayWhenReady()
      .then((initialState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackPlayWhenReadyChanged listener below:
        setPlayWhenReady((state) => state ?? initialState);
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = TrackPlayer.addEventListener(
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

/**
 * Get current playback state and subsequent updates.
 *
 * Note: While it is fetching the initial state from the native module, the
 * returned state property will be `undefined`.
 * */
export const usePlaybackState = (): PlaybackState | { state: undefined } => {
  const [state, setState] = useState<PlaybackState | { state: undefined }>({
    state: undefined,
  });
  useEffect(() => {
    let mounted = true;

    TrackPlayer.getPlaybackState()
      .then((initialState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackState listener below:
        setState((state) => state ?? initialState);
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = TrackPlayer.addEventListener(Event.PlaybackState, (state) => {
      setState(state);
    });

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return state;
};

/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
export const useTrackPlayerEvents = <
  T extends Event[],
  H extends (data: EventsPayloadByEvent[T[number]]) => void
>(
  events: T,
  handler: H
) => {
  const savedHandler = useRef(handler);
  savedHandler.current = handler;

  /* eslint-disable react-hooks/exhaustive-deps */
  useEffect(() => {
    if (__DEV__) {
      const allowedTypes = Object.values(Event);
      const invalidTypes = events.filter(
        (type) => !allowedTypes.includes(type)
      );
      if (invalidTypes.length) {
        console.warn(
          'One or more of the events provided to useTrackPlayerEvents is ' +
            `not a valid TrackPlayer event: ${invalidTypes.join("', '")}. ` +
            'A list of available events can be found at ' +
            'https://react-native-track-player.js.org/docs/api/events'
        );
      }
    }

    const subs = events.map((type) =>
      TrackPlayer.addEventListener(type, (payload) => {
        // @ts-expect-error - we know the type is correct
        savedHandler.current({ ...payload, type });
      })
    );

    return () => subs.forEach((sub) => sub.remove());
  }, events);
};

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param updateInterval - ms interval
 */
export function useProgress(updateInterval = 1000) {
  const INITIAL = {
    position: 0,
    duration: 0,
    buffered: 0,
  };
  const [state, setState] = useState<Progress>(INITIAL);

  useTrackPlayerEvents([Event.PlaybackActiveTrackChanged], () => {
    setState(INITIAL);
  });

  useEffect(() => {
    let mounted = true;

    const update = async () => {
      try {
        const { position, duration, buffered } =
          await TrackPlayer.getProgress();
        if (!mounted) return;

        setState((state) =>
          position === state.position &&
          duration === state.duration &&
          buffered === state.buffered
            ? state
            : { position, duration, buffered }
        );
      } catch {
        // these method only throw while you haven't yet setup, ignore failure.
      }
    };

    const poll = async () => {
      await update();
      if (!mounted) return;
      await new Promise<void>((resolve) => setTimeout(resolve, updateInterval));
      if (!mounted) return;
      poll();
    };

    poll();

    return () => {
      mounted = false;
    };
  }, [updateInterval]);

  return state;
}

function useAppIsInBackground() {
  const [state, setState] = useState<AppStateStatus>('active');

  useEffect(() => {
    const onStateChange = (nextState: AppStateStatus) => {
      setState(nextState);
    };

    AppState.addEventListener('change', onStateChange);

    return () => {
      AppState.removeEventListener('change', onStateChange);
    };
  }, []);
  return state === 'background';
}

/**
 * Hook that returns the current sleep timer state and time left.
 *
 * Note that time left is not updated when the app is in the background.
 *
 * @param updateInterval - ms interval at which the time left is updated.
 * Defaults to 60000 (1 minute).
 */
export function useSleepTimer(updateInterval = 60000) {
  // Avoid updating time left when the app is in the background
  const inactive = useAppIsInBackground();
  const [state, setState] = useState<
    | { time: number; secondsLeft: number }
    | { sleepWhenPlayedToEnd: boolean }
    | undefined
  >(undefined);
  const time = state && 'time' in state ? state.time : undefined;

  const addSecondsLeft = (
    state:
      | { time: number; secondsLeft?: number }
      | { sleepWhenPlayedToEnd: boolean }
      | undefined
  ) =>
    state
      ? 'time' in state
        ? {
            ...state,
            secondsLeft: Math.max(
              0,
              Math.round((state.time - Date.now()) / 1000)
            ),
          }
        : state
      : undefined;

  useTrackPlayerEvents([Event.SleepTimerChanged], (event) => {
    setState(addSecondsLeft(event));
  });

  useEffect(() => {
    let unmounted = false;
    TrackPlayer.getSleepTimer().then((state) => {
      if (unmounted) return;
      setState(addSecondsLeft(state ?? undefined));
    });
    return () => {
      unmounted = true;
    };
  }, []);

  useEffect(() => {
    if (inactive || time === undefined) return;
    const update = () => {
      setState((sleepTimer) => {
        const result = addSecondsLeft(sleepTimer);
        if (result && 'secondsLeft' in result && result.secondsLeft === 0) {
          clear();
        }
        return result;
      });
    };

    // In order to make time update reaching 0 sync with firing of completion,
    // first wait for the next interval to start before starting update loop
    let timeoutId = setTimeout(() => {
      update();
      timeoutId = setInterval(update, updateInterval);
    }, updateInterval - (time % updateInterval));
    const clear = () => clearInterval(timeoutId);
    update();
    return clear;
  }, [inactive, time, updateInterval]);

  return state;
}

export const useActiveTrack = (): Track | undefined => {
  const [track, setTrack] = useState<Track | undefined>();

  // Sets the initial index (if still undefined)
  useEffect(() => {
    let unmounted = false;
    TrackPlayer.getActiveTrack()
      .then((initialTrack) => {
        if (unmounted) return;
        setTrack((track) => track ?? initialTrack ?? undefined);
      })
      .catch(() => {
        // throws when you haven't yet setup, which is fine because it also
        // means there's no active track
      });
    return () => {
      unmounted = true;
    };
  }, []);

  useTrackPlayerEvents(
    [Event.PlaybackActiveTrackChanged],
    async ({ track }) => {
      setTrack(track ?? undefined);
    }
  );

  return track;
};
