import React, {useState} from "react";
import {StyleSheet, Text, View} from "react-native";
import {BottomSheetScrollView} from "@gorhom/bottom-sheet";
import SegmentedControl from '@react-native-segmented-control/segmented-control';
import TrackPlayer, {RepeatMode} from "react-native-track-player";
import {DefaultRepeatMode} from "../services";
import {Spacer} from "./Spacer";

export const OptionStack: React.FC<{ children: React.ReactNode, vertical?: boolean }> = ({children, vertical}) => {
  const childrenArray = React.Children.toArray(children);

  return (
    <View style={vertical ? styles.optionColumn : styles.optionRow}>
      {childrenArray.map((child, index) => (
        <View key={index}>
          {child}
        </View>
      ))}
    </View>
  );
}

export const OptionSheet: React.FC = () => {
  const [selectedRepeatMode, setSelectedRepeatMode] = useState(
    repeatModeToIndex(DefaultRepeatMode)
  );

  return (

    <BottomSheetScrollView contentContainerStyle={styles.contentContainer}>
      <OptionStack vertical={true}>
        <Text style={styles.optionRowLabel}>Repeat Mode</Text>
        <Spacer />
        <SegmentedControl
          appearance={'dark'}
          values={['Off', 'Track', 'Queue']}
          selectedIndex={selectedRepeatMode}
          onChange={async (event) => {
            setSelectedRepeatMode(event.nativeEvent.selectedSegmentIndex);
            const repeatMode = repeatModeFromIndex(event.nativeEvent.selectedSegmentIndex);
            await TrackPlayer.setRepeatMode(repeatMode);
          }}
        />
      </OptionStack>
    </BottomSheetScrollView>
  );
}

const styles = StyleSheet.create({
  contentContainer: {
    flex: 1,
    marginTop: '4%',
    marginHorizontal: 16,
  },
  optionRow: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  optionColumn: {
    width: '100%',
    flexDirection: 'column',
  },
  optionRowLabel: {
    color: 'white',
    fontSize: 20,
    fontWeight: '600',
  }
});

const repeatModeFromIndex = (index: number): RepeatMode => {
  switch (index) {
    case 0:
      return RepeatMode.Off;
    case 1:
      return RepeatMode.Track;
    case 2:
      return RepeatMode.Queue;
    default:
      return RepeatMode.Off;
  }
}

const repeatModeToIndex = (repeatMode: RepeatMode): number => {
  switch (repeatMode) {
    case RepeatMode.Off:
      return 0;
    case RepeatMode.Track:
      return 1;
    case RepeatMode.Queue:
      return 2;
    default:
      return 0;
  }
}
