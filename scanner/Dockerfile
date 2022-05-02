FROM bitnami/python:3.8 as base

ENV FING_VERISON=5.5.2
ENV FING_ARCHIVE_URL=https://www.fing.com/images/uploads/general/CLI_Linux_Debian_5.5.2.zip

FROM base as download
RUN apt update
RUN apt install -y curl unzip
RUN curl -O "$FING_ARCHIVE_URL"
RUN unzip $(basename ${FING_ARCHIVE_URL})


FROM download as fingcli
COPY --from=download /app/fing-5.5.2-amd64.deb ./
COPY conf /etc/fing
RUN dpkg -i fing-5.5.2-amd64.deb
RUN rm fing-5.5.2-amd64.deb

FROM fingcli as dependencies
COPY requirements.txt /app/
RUN pip install -r /app/requirements.txt
RUN rm /app/requirements.txt


FROM dependencies as envvars
ENV SUBNET="192.168.0.1/24"
ENV FILE_LOCATION="/var/log/fing/log.txt"
ENV DNS_SERVER="8.8.8.8"
ENV LOG_SERVER="loki"
ENV INFLUX_SERVER="influx"
ENV INFLUX_TOKEN="influx"
ENV INFLUX_ORG="influx"
ENV INFLUX_BUCKET="influx"


FROM envvars as monitor
COPY run.sh /app
COPY reporter.py /app
RUN chmod +x /app/run.sh

ENTRYPOINT [ "sh", "-c", "/app/run.sh $SUBNET $FILE_LOCATION $DNS_SERVER $LOG_SERVER $INFLUX_SERVER $INFLUX_TOKEN $INFLUX_ORG $INFLUX_BUCKET"]
