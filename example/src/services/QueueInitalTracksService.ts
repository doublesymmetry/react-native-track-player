import TrackPlayer, { RepeatMode } from 'react-native-track-player';

// @ts-expect-error – sure we can import this
import playlistData from '../assets/data/playlist.json';
// @ts-expect-error – sure we can import this
import localTrack from '../assets/resources/pure.m4a';
// @ts-expect-error – sure we can import this
import localArtwork from '../assets/resources/artwork.jpg';

export const QueueInitalTracksService = async (): Promise<void> => {
  await TrackPlayer.add([
    ...playlistData,
    {
      url: localTrack,
      title: 'Pure (Demo)',
      artist: 'David Chavez',
      artwork: localArtwork,
      duration: 28,
    },
  ]);
  await TrackPlayer.setRepeatMode(RepeatMode.Queue);
};
