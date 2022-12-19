import type { Progress } from '../Progress';

export interface PlaybackProgressUpdatedEvent extends Progress {
  track: number;
}
