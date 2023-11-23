#!/bin/bash

if [ -f bootrun.pid ]; then
  pid="$(cat bootrun.pid)"
  kill -9 $pid
  rm bootrun.pid
fi
# ./gradlew build -x test --continuous > continue.log 2>&1 &
./gradlew build -x test --continuous &
echo $1 > gradle_continue.pid
# ./gradlew bootRun > output.log 2>&1 &
./gradlew bootRun &
echo $! > bootrun.pid