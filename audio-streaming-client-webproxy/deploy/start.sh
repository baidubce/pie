#! /bin/bash

rm -rf nohup.out

nohup java -jar -Dspring.config.loction=conf/application.conf -XX:MaxDirectMemorySize=1024M audio-streaming-webproxy.jar

echo $! > pidfile.tem