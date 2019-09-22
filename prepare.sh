#!/bin/bash

sudo apt-get update
sudo apt-get install openjdk-8-jdk
git clone https://github.com/faucct/yandex_cloud-akka-aeron
cd yandex_cloud-akka-aeron
./gradlew build
