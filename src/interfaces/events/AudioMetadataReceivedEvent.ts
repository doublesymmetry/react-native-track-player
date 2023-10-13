export interface AudioMetadataReceivedEvent {
  metadata: AudioMetadata[];
}

export interface AudioCommonMetadataReceivedEvent {
  metadata: AudioCommonMetadata;
}

export interface AudioCommonMetadata {
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
}

export interface AudioMetadata extends AudioCommonMetadata {
  raw: RawEntry[];
}

export interface RawEntry {
  commonKey: string | undefined;
  keySpace: string | undefined;
  time: number | undefined;
  value: unknown | null;
  key: string;
}
