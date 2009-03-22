#!/bin/bash

while read -a query
do
    index=0
    category=${query[0]}
    unset query[0]
    echo "<query>${query[@]}</query>" >> $category
done
