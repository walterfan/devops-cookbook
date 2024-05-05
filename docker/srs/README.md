# Overview

* start SRS5 docker container

```sh
docker-compose up -d
```

* push stream to srs

```sh

gst-launch-1.0 -vv filesrc location=digits.mp4 ! decodebin ! videoconvert ! identity drop-allocation=1 ! x264enc tune=zerolatency ! flvmux streamable=true ! rtmpsink location='rtmp://192.168.104.236:1935/live/waltertest
```


然后就可以通过 http://192.168.104.236:1975/live/waltertest.flv 进行播放了