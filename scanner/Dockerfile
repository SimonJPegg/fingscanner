FROM mozilla/sbt:8u292_1.5.7 as build
COPY project /app/project
COPY src /app/src
COPY build.sbt /app/build.sbt
WORKDIR /app
RUN sbt clean assembly

FROM adoptopenjdk/openjdk11:jre-11.0.11_9 as base
ENV FING_VERISON=5.5.2
ENV FING_ARCHIVE_URL=https://www.fing.com/images/uploads/general/CLI_Linux_Debian_5.5.2.zip

FROM base as download
RUN apt update
RUN apt install -y curl unzip
RUN curl -O "$FING_ARCHIVE_URL"
RUN unzip $(basename ${FING_ARCHIVE_URL})

FROM download as fingcli
COPY --from=download fing-5.5.2-amd64.deb ./
COPY conf /etc/fing
RUN dpkg -i fing-5.5.2-amd64.deb
RUN rm fing-5.5.2-amd64.deb

FROM fingcli as App
COPY --from=build /app/target/scala-2.13/fingscanner-0.1-jar-with-dependencies.jar /app/fingscanner.jar
ENV SUBNET="192.168.0.1/24"
ENV INFLUX_SERVER="influx"
ENV INFLUX_TOKEN="influx"
ENV INFLUX_ORG="influx"
ENV INFLUX_BUCKET="influx"
ENV DNS_SERVER="8.8.8.8"
ENV LOG_SERVER="loki"
ENV ACTOR_POOL="10"
ENV SCHEDULE="10"


ENTRYPOINT [ "sh", "-c", "java -jar /app/fingscanner.jar --subnet $SUBNET --influx-server $INFLUX_SERVER --influx-token $INFLUX_TOKEN --influx-org $INFLUX_ORG --influx-bucket $INFLUX_BUCKET --log-server $LOG_SERVER --dns-server $DNS_SERVER --actor-pool $ACTOR_POOL --interval $SCHEDULE"]
