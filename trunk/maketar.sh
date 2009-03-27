#!/bin/bash

mkdir ans2120-proj2
if [ $? != 0 ]; then
    exit 1
fi

cp -R src Readme.pdf build.sh run.sh maketar.sh Makefile ans2120-proj2

tar czvf ans2120-proj2.tar.gz ans2120-proj2

rm -rf ans2120-proj2
