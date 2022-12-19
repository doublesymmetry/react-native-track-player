const path = require('path');
const {CracoAliasPlugin} = require('react-app-alias-ex');
const babelInclude = require('@dealmore/craco-plugin-babel-include');
const webpack = require('webpack');

module.exports = {
  webpack: {
    alias: {
      'react-native$': 'react-native-web',
      'react-native-track-player': path.resolve(__dirname, '../'),
      // make sure we don't include multiple versions of react
      'react': path.resolve(__dirname, './node_modules/react'),
    },
    plugins: {
        add: [
          new webpack.EnvironmentPlugin({ JEST_WORKER_ID: null }),
          new webpack.DefinePlugin({
            process: { env: {} },
            __DEV__: true,
          })
        ],
      },
  },
  babel: {
    presets: [
      '@babel/preset-react',
      '@babel/preset-typescript',
    ],
  },
  plugins: [
    {
        plugin: CracoAliasPlugin,
        options: {}
    },
    {
      plugin: babelInclude,
      options: {
        include: [
          path.resolve(__dirname, '../'),
        ],
      },
    },
  ],
};
