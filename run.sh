#!/bin/sh

echo "input start time"
read start

echo "input end time"
read end

sbt "run $start $end" &
echo "input file path"

touch buffer
./$path > buffer

