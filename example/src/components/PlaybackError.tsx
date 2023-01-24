import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

export const PlaybackError: React.FC<{
  error?: string;
}> = ({ error }) => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>{error}</Text>
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
