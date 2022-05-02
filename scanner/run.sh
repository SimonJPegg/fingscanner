#!/bin/bash

SUBNET="$1"
FILE_LOCATION="$2"
DNS_SERVER="$3"
LOG_SERVER="$4"
INFLUX_SERVER="$5"
INFLUX_TOKEN="$6"
INFLUX_ORG="$7"
INFLUX_BUCKET="$8"


touch "$FILE_LOCATION"
echo "Watching for changes"
python /app/reporter.py -s "$SUBNET" -f "$FILE_LOCATION" -d "$DNS_SERVER" -i "$INFLUX_SERVER"  -l "$LOG_SERVER" -t "$INFLUX_TOKEN" -o "$INFLUX_ORG"  -b "$INFLUX_BUCKET"
#echo "Logging for subnet $SUBNET to $FILE_LOCATION"
#/usr/bin/fing "$SUBNET" -d on -o table,csv,"$FILE_LOCATION"
# echo "Shouldn't ever get here"


