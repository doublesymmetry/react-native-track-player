#!/bin/bash

set -e

###################
# BEFORE CACHE    #
###################
case "${TRAVIS_OS_NAME}" in
  linux)
    rm -rf "$HOME/.gradle/caches/modules-2/modules-2.lock"
  ;;
esac

###################
# BEFORE INSTALL  #
###################
export NODEJS_ORG_MIRROR=http://nodejs.org/dist

wget https://raw.githubusercontent.com/creationix/nvm/v0.31.0/nvm.sh -O ~/.nvm/nvm.sh
source ~/.nvm/nvm.sh

nvm install 9.11.2

npm install -g react-native-cli

npm install

# Remove existing tarball
rm -rf *.tgz

npm pack

###################
# INSTALL         #
###################
cd example || exit

rm -rf node_modules && npm install

###################
# BEFORE CI       #
###################
case "${TRAVIS_OS_NAME}" in
  linux)
    echo no | android create avd --force -n test -t android-21 --abi armeabi-v7a --skin WVGA800
    emulator -avd test -scale 96dpi -dpi-device 160 -no-audio -no-window &
    android-wait-for-emulator
    sleep 60
    adb shell input keyevent 82 &
  ;;
esac

npm run appium > /dev/null 2>&1 &

###################
# CI              #
###################
case "${TRAVIS_OS_NAME}" in
  osx)
    npm run build:ios | xcpretty -c -f `xcpretty-travis-formatter`
    npm run test:ios
  ;;
  linux)
    npm run build:android
    npm run test:android
  ;;
esac