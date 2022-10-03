import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { PlaybackState, State } from 'react-native-track-player';

export const PlaybackError: React.FC<{ playbackState: PlaybackState }> = ({
  playbackState,
}) => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {
          (playbackState.state === State.Error
            ? playbackState.error
            : undefined
          )?.message
        }
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
  },
  text: {
    color: 'red',
    width: '100%',
    textAlign: 'center',
  },
});
