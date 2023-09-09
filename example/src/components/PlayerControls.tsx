import React from 'react';
import { StyleSheet, TouchableWithoutFeedback, View } from 'react-native';
import TrackPlayer, { usePlaybackState } from 'react-native-track-player';
import FontAwesome6 from 'react-native-vector-icons/FontAwesome6';

import { PlaybackError } from './PlaybackError';
import { PlayPauseButton } from './PlayPauseButton';
import { FadeEvent } from '../constant';

const performSkipToNext = () =>
  TrackPlayer.setAnimatedVolume({
    volume: 0,
    msg: FadeEvent.FadeNext,
  });
const performSkipToPrevious = () =>
  TrackPlayer.setAnimatedVolume({
    volume: 0,
    msg: FadeEvent.FadePrevious,
  });

export const PlayerControls: React.FC = () => {
  const playback = usePlaybackState();
  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <TouchableWithoutFeedback onPress={performSkipToPrevious}>
          <FontAwesome6 name={'backward'} size={30} color={'white'} />
        </TouchableWithoutFeedback>
        <PlayPauseButton />
        <TouchableWithoutFeedback onPress={performSkipToNext}>
          <FontAwesome6 name={'forward'} size={30} color={'white'} />
        </TouchableWithoutFeedback>
      </View>
      <PlaybackError
        error={'error' in playback ? playback.error.message : undefined}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
  },
});
