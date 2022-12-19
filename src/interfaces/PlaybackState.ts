import { State } from '../constants';
import type { PlaybackErrorEvent } from './events';

export type PlaybackState =
  | {
      state:
        | State.None
        | State.Buffering
        | State.Loading
        | State.Playing
        | State.Paused
        | State.Ready
        | State.Paused
        | State.Stopped;
    }
  | {
      state: State.Error;
      error: PlaybackErrorEvent;
    };
