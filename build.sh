#!/usr/bin/env bash

docker build -t registry.undernet.antipathy.org/fingscanner:latest scanner
docker push registry.undernet.antipathy.org/fingscanner:latest
