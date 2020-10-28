#!/bin/bash
file=$1
mkdir lines
cd lines
split -a 10 -1 $file
index=1
for i in $(ls x*)
    do
        mv $i $index.txt
        index=$(($index + 1))
    done

