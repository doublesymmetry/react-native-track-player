import React, { useEffect } from "react";
import { StyleSheet, Text, View } from "react-native";
import TrackPlayer, { Capability, State, usePlaybackState } from "react-native-track-player";

import Player from "../components/Player";
import playlistData from "../data/playlist.json";
import localTrack from "../resources/pure.m4a";

export default function LandingScreen() {
  const playbackState = usePlaybackState();

  useEffect(() => {
    TrackPlayer.setupPlayer();
    TrackPlayer.updateOptions({
      stopWithApp: true,
      capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.Stop
      ],
      compactCapabilities: [
        Capability.Play,
        Capability.Pause
      ]
    });
  }, []);

  async function togglePlayback() {
    const currentTrack = await TrackPlayer.getCurrentTrack();
    if (currentTrack == null) {
      await TrackPlayer.reset();
      await TrackPlayer.add(playlistData);
      await TrackPlayer.add({
        id: "local-track",
        url: localTrack,
        title: "Pure (Demo)",
        artist: "David Chavez",
        artwork: "https://picsum.photos/200"
      });
      await TrackPlayer.play();
    } else {
      if (playbackState === State.Paused) {
        await TrackPlayer.play();
      } else {
        await TrackPlayer.pause();
      }
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.description}>
        We'll be inserting a playlist into the library loaded from
        `playlist.json`. We'll also be using the `ProgressComponent` which
        allows us to track playback time.
      </Text>
      <Player
        onNext={skipToNext}
        style={styles.player}
        onPrevious={skipToPrevious}
        onTogglePlayback={togglePlayback}
      />
      <Text style={styles.state}>{getStateName(playbackState)}</Text>
    </View>
  );
}

LandingScreen.navigationOptions = {
  title: "Playlist Example"
};

function getStateName(state) {
  switch (state) {
    case State.None:
      return "None";
    case State.Playing:
      return "Playing";
    case State.Paused:
      return "Paused";
    case State.Stopped:
      return "Stopped";
    case State.Buffering:
      return "Buffering";
  }
}

async function skipToNext() {
  try {
    await TrackPlayer.skipToNext();
  } catch (_) {}
}

async function skipToPrevious() {
  try {
    await TrackPlayer.skipToPrevious();
  } catch (_) {}
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  description: {
    width: "80%",
    marginTop: 20,
    textAlign: "center"
  },
  player: {
    marginTop: 40
  },
  state: {
    marginTop: 20
  }
});
