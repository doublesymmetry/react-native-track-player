import TrackPlayer, { type Track } from 'react-native-track-player';

import playlistData from '@/assets/data/playlist.json';

export const QueueInitialTracksService = async (): Promise<void> => {
  await TrackPlayer.add(playlistData as Track[]);
};
