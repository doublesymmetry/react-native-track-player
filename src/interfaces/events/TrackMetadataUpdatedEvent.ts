import type { Track } from '../Track';

export interface TrackMetadataUpdatedEvent {
  index?: number;
  track?: Track;
}
