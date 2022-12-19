export interface PlaybackMetadataReceivedEvent {
  /** The metadata source  */
  source: string;
  /** The track title */
  title: string | null;
  /** The track url */
  url: string | null;
  /** The track artist */
  artist: string | null;
  /** The track album */
  album: string | null;
  /** The track date */
  date: string | null;
  /** The track genre */
  genre: string | null;
}
