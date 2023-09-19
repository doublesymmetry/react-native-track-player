export interface AudioCommonMetadataReceivedEvent {
  title: string | undefined;
  artist: string | undefined;
  albumTitle: string | undefined;
  subtitle: string | undefined;
  description: string | undefined;
  artworkUri: string | undefined;
  trackNumber: string | undefined;
  composer: string | undefined;
  conductor: string | undefined;
  genre: string | undefined;
  compilation: string | undefined;
  station: string | undefined;
  mediaType: string | undefined;
  creationDate: string | undefined;
  creationYear: string | undefined;
  raw: RawEntry[];
}

export interface RawEntry {
  commonKey: string | undefined;
  keySpace: string | undefined;
  time: number | undefined;
  value: unknown | null;
  key: string;
}
