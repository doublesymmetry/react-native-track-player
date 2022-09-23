import { useState } from 'react';
import {
  Event,
  PlaybackErrorEvent,
  useTrackPlayerEvents,
} from 'react-native-track-player';

export function usePlaybackError() {
  const [error, setError] = useState<PlaybackErrorEvent>();
  useTrackPlayerEvents(
    [Event.PlaybackError, Event.PlaybackState, Event.PlaybackTrackChanged],
    (event) => {
      console.log(event);
      if (event.type === Event.PlaybackError) {
        setError(event);
      }
      if (event.type === Event.PlaybackTrackChanged) {
        setError(undefined);
      }
    }
  );
  return error;
}
