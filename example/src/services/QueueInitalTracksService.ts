import TrackPlayer, {RepeatMode} from 'react-native-track-player';

// @ts-ignore
import playlistData from '../assets/data/playlist.json';
// @ts-ignore
import localTrack from '../assets/resources/pure.m4a';
// @ts-ignore
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
