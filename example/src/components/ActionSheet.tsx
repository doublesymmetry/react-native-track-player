import React, { useEffect } from 'react';
import { Spacer } from './Spacer';
import { Button } from './Button';
import TrackPlayer from 'react-native-track-player';
import { BottomSheetScrollView } from '@gorhom/bottom-sheet';
import { Image } from 'react-native';

const ARTWORK_URLS = [
  'https://images.unsplash.com/photo-1503023345310-bd7c1de61c7d',
  'https://images.unsplash.com/photo-1521747116042-5a810fda9664',
  'https://images.unsplash.com/photo-1516117172878-fd2c41f4a759',
  'https://images.unsplash.com/photo-1532009324734-20a7a5813719',
  'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e',
  'https://images.unsplash.com/photo-1508780709619-79562169bc64',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330',
  'https://images.unsplash.com/photo-1541696432-82c6da8ce7bf',
  'https://images.unsplash.com/photo-1517841905240-472988babdf9',
  'https://images.unsplash.com/photo-1524504388940-b1c1722653e1',
];

const getRandomArtwork = () => {
  const randomIndex = Math.floor(Math.random() * ARTWORK_URLS.length);
  return ARTWORK_URLS[randomIndex];
};

// Merges current metadata with random title & artwork.
// artist remains unchanged, because no artist value is provided.
const onUpdateNotificationMetadata = async () => {
  const randomTitle = Math.random().toString(36).substring(7);
  await TrackPlayer.updateNowPlayingMetadata({
    title: `Random: ${randomTitle}`,
    artwork: getRandomArtwork(),
  });
};

const onUpdateCurrentTrackMetadata = async () => {
  const currentTrackIndex = await TrackPlayer.getActiveTrackIndex();
  if (currentTrackIndex !== undefined) {
    const randomTitle = Math.random().toString(36).substring(7);
    await TrackPlayer.updateMetadataForTrack(currentTrackIndex, {
      title: `Random: ${randomTitle}`,
      artwork: getRandomArtwork(),
      duration: Math.floor(Math.random()),
    });
  }
};

// Removes artwork and artist for the current track, by setting them to empty strings.
const onRemoveArtworkAndArtist = async () => {
  const currentTrackIndex = await TrackPlayer.getActiveTrackIndex();
  if (currentTrackIndex !== undefined) {
    await TrackPlayer.updateMetadataForTrack(currentTrackIndex, {
      artwork: '',
      artist: '',
    });
  }
};

const onReset = async () => {
  await TrackPlayer.reset();
};

export const ActionSheet: React.FC = () => {
  useEffect(() => {
    const preloadImages = async () => {
      try {
        const preloadPromises = ARTWORK_URLS.map((url) => Image.prefetch(url));
        await Promise.all(preloadPromises);
      } catch (error) {
        console.error('Error preloading images:', error);
      }
    };

    preloadImages();
  }, []);

  return (
    <BottomSheetScrollView>
      <Spacer />
      <Button
        title={'Update Now Playing Metadata Randomly'}
        onPress={onUpdateNotificationMetadata}
        type={'primary'}
      />
      <Button
        title={'Update Current Track Metadata Randomly'}
        onPress={onUpdateCurrentTrackMetadata}
        type={'primary'}
      />
      <Button
        title={'Remove Artwork And Artist For Current Track'}
        onPress={onRemoveArtworkAndArtist}
        type={'primary'}
      />
      <Button title={'Reset'} onPress={onReset} type={'primary'} />
    </BottomSheetScrollView>
  );
};
