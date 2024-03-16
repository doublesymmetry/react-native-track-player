import type { NowPlayingMetadata } from "../NowPlayingMetadata";

export interface NowPlayingMetadataChangedEvent {
  metadata: NowPlayingMetadata;
}
