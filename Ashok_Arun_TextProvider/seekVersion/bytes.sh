#!/bin/bash 
file=$1
echo -n "0" > linebytes.txt
numBytes=0
lines=1
while read line
do
    numBytes=$((numBytes + $(sed -n ${lines}p $file | wc -c)))
    echo -n " $numBytes" >> linebytes.txt
    lines=$((lines+1))
done < $file
exit 0
