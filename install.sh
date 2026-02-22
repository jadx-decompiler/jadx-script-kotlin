#!/usr/bin/env bash

./gradlew clean dist

BUNDLE_FILE="build/dist/jadx-script-kotlin-dev.zip"

# check file size
ls -lh $BUNDLE_FILE

jadx plugins --install "file:$BUNDLE_FILE"

jadx plugins -l
