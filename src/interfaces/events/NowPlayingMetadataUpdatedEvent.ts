import type { Track } from '../Track';

export interface NowPlayingMetadataUpdatedEvent {
  index?: number;
  track?: Track;
}
