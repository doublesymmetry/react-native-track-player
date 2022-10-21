import { useEffect, useState } from "react";
import { Event } from "../constants";
import { getSleepTimer } from '../trackPlayer';
import { useAppIsInBackground } from "./useAppIsInBackground";
import { useTrackPlayerEvents } from "./useTrackPlayerEvents";

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
    getSleepTimer().then((state) => {
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
