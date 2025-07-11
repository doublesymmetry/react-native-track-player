import React from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import TrackPlayer, { Track, useIsPlaying } from 'react-native-track-player';
import Icon from '@expo/vector-icons/FontAwesome6';
import playlistData from '@/assets/data/playlist.json';

export const PlayPauseButton: React.FC = () => {
  const { playing, bufferingDuringPlay } = useIsPlaying();

  async function playTrack() {
    try {
      const queue = await TrackPlayer.getQueue();
      if (!queue.length) {
        await TrackPlayer.add(playlistData as Track[]);
      }
      await TrackPlayer.play();
    } catch (error) {
      console.log('Error adding track:', error);
    }
  }

  return (
    <View style={styles.container}>
      {bufferingDuringPlay ? (
        <ActivityIndicator />
      ) : (
        <TouchableWithoutFeedback
          onPress={playing ? TrackPlayer.pause : playTrack}
        >
          <Icon
            name={playing ? 'pause' : 'play'}
            size={48}
            color="white"
            iconStyle="solid"
          />
        </TouchableWithoutFeedback>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 50,
    width: 120,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
