#!/bin/sh

echo "input start time"
read start

echo "input end time"
read end

echo "input file path"
read path

sbt "run $start $end" &

touch buffer
./$path > buffer

