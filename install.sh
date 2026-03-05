#!/usr/bin/env bash

export JADX_SCRIPT_KOTLIN_PLUGIN_VERSION="1.0.0"
./gradlew clean dist

BUNDLE_FILE="build/dist/jadx-script-kotlin-1.0.0.zip"

# check file size
ls -lh $BUNDLE_FILE

jadx plugins --install "file:$BUNDLE_FILE"

jadx plugins -l
