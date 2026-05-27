import './global.css';

import { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  StatusBar,
  View,
  useColorScheme,
} from 'react-native';
import type { EmitterSubscription } from 'react-native';
import type { NativeStackNavigationOptions } from '@react-navigation/native-stack';
import Ionicons from 'react-native-vector-icons/Ionicons';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import TrackPlayer, {
  DEFAULT_CAST_RECEIVER_APP_ID,
  Event,
  useActiveMediaItem,
} from '@rntp/player';
import { useEventLogStore } from './stores/eventLog';
import { useDownloadedTrackStore } from './stores/downloadedTrack';
import { ensureDownloadedTrack } from './lib/downloadedTracks';
import { FILE_TRACK_RESOURCE_NAME, FILE_TRACK_RESOURCE_EXT } from './data/music';

import MusicScreen from './screens/MusicScreen';
import PodcastScreen from './screens/PodcastScreen';
import RadioScreen from './screens/RadioScreen';
import SettingsScreen from './screens/SettingsScreen';
import PlayerScreen from './screens/PlayerScreen';
import MiniPlayer from './components/MiniPlayer';
import type { RootStackParamList, TabParamList } from './navigation/types';
import { StrongSongsFeedProvider } from './contexts/StrongSongsFeedContext';
import { useStrongSongsFeed } from './hooks/useStrongSongsFeed';

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<TabParamList>();

const TAB_BAR_HEIGHT = Platform.OS === 'ios' ? 50 : 56;
export const MINI_PLAYER_HEIGHT = 64;

const lightTheme = {
  dark: false,
  colors: {
    primary: 'hsl(152, 60%, 42%)',
    background: 'hsl(0, 0%, 98%)',
    card: 'hsl(0, 0%, 100%)',
    text: 'hsl(0, 0%, 6%)',
    border: 'hsl(0, 0%, 90%)',
    notification: 'hsl(0, 84%, 60%)',
  },
};

const darkTheme = {
  dark: true,
  colors: {
    primary: 'hsl(152, 60%, 42%)',
    background: 'hsl(0, 0%, 5%)',
    card: 'hsl(0, 0%, 10%)',
    text: 'hsl(0, 0%, 96%)',
    border: 'hsl(0, 0%, 16%)',
    notification: 'hsl(0, 63%, 31%)',
  },
};

type TabIconProps = { color: string; focused: boolean };
const MusicTabIcon = ({ color, focused: _focused }: TabIconProps) => (
  <Ionicons name="musical-notes" size={22} color={color} />
);
const PodcastsTabIcon = ({ color, focused: _focused }: TabIconProps) => (
  <Ionicons name="mic" size={22} color={color} />
);
const RadioTabIcon = ({ color, focused: _focused }: TabIconProps) => (
  <Ionicons name="radio" size={22} color={color} />
);
const SettingsTabIcon = ({ color, focused: _focused }: TabIconProps) => (
  <Ionicons name="settings-outline" size={22} color={color} />
);

const playerScreenOptions: NativeStackNavigationOptions = {
  headerShown: false,
  gestureEnabled: true,
  ...(Platform.OS === 'ios'
    ? { presentation: 'modal' }
    : {
        presentation: 'fullScreenModal',
        animation: 'slide_from_bottom',
      }),
};

export default function App() {
  return (
    <StrongSongsFeedProvider>
      <AppContent />
    </StrongSongsFeedProvider>
  );
}

function AppContent() {
  const scheme = useColorScheme();
  const [isReady, setIsReady] = useState(false);
  useStrongSongsFeed(); // triggers browse tree setup

  useEffect(() => {
    try {
      TrackPlayer.setupPlayer({
        contentType: 'music',
        handleAudioBecomingNoisy: true,
        cache: {},
        progressSync: {
          intervalSeconds: 5,
          http: {
            url: 'http://localhost:3333/progress',
          },
        },
        android: {
          wakeMode: 'network',
          skipSilenceEnabled: false,
          cast: DEFAULT_CAST_RECEIVER_APP_ID,
        },
      });
      setIsReady(true);
    } catch (error) {
      if ((error as Error).message?.includes('already set up')) {
        setIsReady(true);
      } else {
        console.error('Failed to setup player:', error);
      }
    }
  }, []);

  // Copy a native resource to Documents so the file:// demo track is available.
  useEffect(() => {
    if (!isReady) return;
    let cancelled = false;
    ensureDownloadedTrack(FILE_TRACK_RESOURCE_NAME, FILE_TRACK_RESOURCE_EXT).then(uri => {
      if (!cancelled) useDownloadedTrackStore.getState().setFileUri(uri);
    });
    return () => {
      cancelled = true;
    };
  }, [isReady]);

  // iOS: all events here. Android: foreground here; background → TaskService (index.js).
  useEffect(() => {
    if (Platform.OS !== 'ios' || !isReady) return;
    const addLog = useEventLogStore.getState().addLog;
    const subs: EmitterSubscription[] = Object.values(Event).map(event =>
      TrackPlayer.addEventListener(event as any, (payload: any) => {
        addLog(event, payload ?? {});
      }),
    );
    return () => subs.forEach(s => s.remove());
  }, [isReady]);

  if (!isReady) {
    return (
      <View className="flex-1 items-center justify-center bg-background">
        <ActivityIndicator size="large" />
      </View>
    );
  }

  return (
    <GestureHandlerRootView className="flex-1">
      <SafeAreaProvider>
        <StatusBar
          barStyle={scheme === 'dark' ? 'light-content' : 'dark-content'}
          translucent
          backgroundColor="transparent"
        />
        <NavigationContainer theme={scheme === 'dark' ? darkTheme : lightTheme}>
          <Stack.Navigator>
            <Stack.Screen
              name="Main"
              component={MainTabsWithPlayer}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="Player"
              component={PlayerScreen}
              options={playerScreenOptions}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

function MainTabsWithPlayer({ navigation }: { navigation: any }) {
  const activeMediaItem = useActiveMediaItem();
  const insets = useSafeAreaInsets();
  const hasPlayer = activeMediaItem != null;
  const sceneContainerStyle = useMemo(
    () => ({ paddingBottom: hasPlayer ? MINI_PLAYER_HEIGHT : 0 }),
    [hasPlayer],
  );

  return (
    <View className="flex-1">
      <Tab.Navigator
        screenOptions={{
          headerShown: false,
          tabBarActiveTintColor: lightTheme.colors.primary,
          tabBarInactiveTintColor: 'rgba(128,128,128,0.6)',
          tabBarStyle: {
            height: TAB_BAR_HEIGHT + insets.bottom,
            paddingBottom: insets.bottom,
            borderTopWidth: 0.5,
            borderTopColor: 'rgba(128,128,128,0.2)',
            elevation: 0,
          },
          tabBarLabelStyle: {
            fontSize: 10,
            fontWeight: '600',
            marginTop: -2,
          },
          tabBarIconStyle: {
            marginBottom: -2,
          },
        }}
        sceneContainerStyle={sceneContainerStyle}
      >
        <Tab.Screen
          name="Music"
          component={MusicScreen}
          options={{ tabBarIcon: MusicTabIcon }}
        />
        <Tab.Screen
          name="Podcasts"
          component={PodcastScreen}
          options={{ tabBarIcon: PodcastsTabIcon }}
        />
        <Tab.Screen
          name="Radio"
          component={RadioScreen}
          options={{ tabBarIcon: RadioTabIcon }}
        />
        <Tab.Screen
          name="Settings"
          component={SettingsScreen}
          options={{ tabBarIcon: SettingsTabIcon }}
        />
      </Tab.Navigator>

      {hasPlayer && (
        <View
          className="absolute left-0 right-0"
          style={{ bottom: TAB_BAR_HEIGHT + insets.bottom }}
        >
          <MiniPlayer onPress={() => navigation.navigate('Player')} />
        </View>
      )}
    </View>
  );
}
