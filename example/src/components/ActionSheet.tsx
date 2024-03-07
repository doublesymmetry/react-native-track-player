import React from 'react';
import { ScrollView } from 'react-native';
import { Spacer } from './Spacer';
import { Button } from './Button';
import TrackPlayer from 'react-native-track-player';

const onUpdateNotificationMetadata = async () => {
  const randomTitle = Math.random().toString(36).substring(7);
  await TrackPlayer.updateNowPlayingMetadata({
    title: `Random: ${randomTitle}`,
    artwork: `https://random.imagecdn.app/800/800?dummy=${Date.now()}`,
  });
};

const onUpdateCurrentTrackMetadata = async () => {
  const currentTrackIndex = await TrackPlayer.getActiveTrackIndex();
  if (currentTrackIndex !== undefined) {
    const randomTitle = Math.random().toString(36).substring(7);
    await TrackPlayer.updateMetadataForTrack(currentTrackIndex, {
      title: `Random: ${randomTitle}`,
      artwork: `https://random.imagecdn.app/800/800?dummy=${Date.now()}`,
      duration: Math.floor(Math.random()),
    });
  }
};

const onReset = async () => {
  await TrackPlayer.reset();
};

export const ActionSheet: React.FC = () => {
  return (
    <ScrollView>
      <Spacer />
      <Button
        title={'Update Notification Metadata Randomly'}
        onPress={onUpdateNotificationMetadata}
        type={'primary'}
      />
      <Button
        title={'Update Current Track Metadata Randomly'}
        onPress={onUpdateCurrentTrackMetadata}
        type={'primary'}
      />
      <Button title={'Reset'} onPress={onReset} type={'primary'} />
    </ScrollView>
  );
};
