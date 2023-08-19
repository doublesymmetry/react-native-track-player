import React from 'react';
import { BottomSheetScrollView } from '@gorhom/bottom-sheet';
import { StyleSheet } from 'react-native';
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
    });
  }
};

const onReset = async () => {
  await TrackPlayer.reset();
};

export const ActionSheet: React.FC = () => {
  return (
    <BottomSheetScrollView contentContainerStyle={styles.contentContainer}>
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
    </BottomSheetScrollView>
  );
};

const styles = StyleSheet.create({
  contentContainer: {
    flex: 1,
    marginTop: '4%',
    marginHorizontal: 16,
  },
  optionRowLabel: {
    color: 'white',
    fontSize: 20,
    fontWeight: '600',
  },
});
