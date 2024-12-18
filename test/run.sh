#!/bin/bash
set -x

targets="key dafny verifast"

cd `dirname $0`
pwd

cd ..
./gradlew shadowJar
cd test

for i in $targets
do
    echo "Working on $i"
    rm -rf $i
    mkdir -p $i
    java -jar ../build/libs/contractlib-exporter-1.0-SNAPSHOT-all.jar -t $i -d $i -v "$1"

    echo "Working on $i.inner"
    rm -rf $i.inner
    mkdir -p $i.inner
    java -jar ../build/libs/contractlib-exporter-1.0-SNAPSHOT-all.jar -t $i -d $i.inner -v -o inner "$1"
done


