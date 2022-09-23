export interface RemotePlaySearchEvent {
  query: string;
  focus: 'artist' | 'album' | 'playlist' | 'genre';
  title: string;
  artist: string;
  album: string;
  date: string;
  playlist: string;
}
