const path = require('path');
const {CracoAliasPlugin} = require('react-app-alias-ex');
const babelInclude = require('@dealmore/craco-plugin-babel-include');
const webpack = require('webpack');
const { loaderByName, addBeforeLoader } = require('@craco/craco');


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
    configure: (config) => {
      // BEGIN: resolve react-native-vector-icons font files
      config.resolve.extensions.push('.ttf');
      addBeforeLoader(
        config,
        loaderByName('url-loader'),
        {
          test: /\.ttf$/,
          loader: 'url-loader', // or directly file-loader
          include: path.resolve(__dirname, 'node_modules/react-native-vector-icons'),
        },
      );
      // END: resolve react-native-vector-icons font files

      return config;
    },
  },
  babel: {
    presets: [
      '@babel/preset-react',
      '@babel/preset-typescript',
    ],
    plugins: [
      '@babel/plugin-proposal-export-namespace-from',
      'react-native-reanimated/plugin',
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
