import type { TrackMetadataBase } from './TrackMetadataBase';

export interface NowPlayingMetadata extends TrackMetadataBase {
  elapsedTime?: number;
}
