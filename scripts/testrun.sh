#!/bin/sh
set -eu
java -version
javac -version
./gradlew -version
./gradlew check
