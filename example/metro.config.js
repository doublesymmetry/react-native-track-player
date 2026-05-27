const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
const { withNativeWind } = require('nativewind/metro');

// Public example: @rntp/player comes from npm (no monorepo link to repo root).
module.exports = withNativeWind(
  mergeConfig(getDefaultConfig(__dirname), {}),
  { input: './src/global.css', inlineRem: 16 },
);
