import React from "react";
import { createStackNavigator, createAppContainer } from "react-navigation";

import LandingScreen from "./react/screens/LandingScreen";
import PlaylistScreen from "./react/screens/PlaylistScreen";

const AppNavigator = createStackNavigator(
  {
    Landing: {
      screen: LandingScreen
    },
    Playlist: {
      screen: PlaylistScreen
    }
  },
  { initialRouteName: "Landing" }
);

const AppContainer = createAppContainer(AppNavigator);

export default function App() {
  return <AppContainer />;
}
