#!/bin/bash
counter=1
max=$(($1+1))
#rm time.txt
counter=1
while [ $counter -lt $max ]; do
    ((time curl -s --output /dev/null "localhost:$3/get/[1-$2]") >> "time.txt" 2>&1 );
    counter=$(( counter+1 ))
done
exit 0
