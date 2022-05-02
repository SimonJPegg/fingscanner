import sys
import time
import pandas
import dns.resolver
import os
import json
import requests
import click
import copy
import schedule
from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
from os.path import exists
from dns.resolver import NXDOMAIN
from loguru import logger

logger.remove()
logger.add(sys.stderr, level="INFO")


class NetworkScanner:

    def __init__(self, subnet, log_file, dns_server, log_server, influx_server,
                 influx_token, influx_org, influx_bucket):
        super().__init__()
        self._subnet = subnet
        self._src_path = log_file
        resolver = dns.resolver.Resolver()
        self._dns_resolver = resolver
        resolver.nameservers = [dns_server, '8.8.8.8']
        logger.info(f"Monitor created")
        self._log_server = log_server
        self._influx_server = influx_server
        self._influx_token = influx_token
        self._influx_org = influx_org
        self._influx_bucket = influx_bucket

    def _run_scan(self):
        stream = os.popen(f"/usr/bin/fing -r 1 -n {self._subnet} -d on -o table,csv,{self._src_path}")
        for line in stream.readlines():
            logger.info(line)

    def _map_dns_names(self, ip_address):
        addr = ip_address.split('.')
        addr.reverse()
        query = f"{'.'.join(addr)}.in-addr.arpa"
        try:
            result = [a.to_text() for a in self._dns_resolver.resolve(query, 'PTR')][0]
            logger.debug(f"found: {result}")
            return result
        except NXDOMAIN:
            logger.debug(f"no result for: {ip_address}")
            return "unkown.host."

    @staticmethod
    def _read_changes(file):
        counter = 0
        while not exists(file):
            if counter > 6:
                logger.error(f"Could not read {file}, after 1 min")
                raise FileNotFoundError(file)
            time.sleep(10)
            counter += 1
        df = pandas.read_csv(file, sep=';', header=None)
        result = df.rename(columns={
            df.columns[0]: 'IpAddress',
            df.columns[1]: 'CustomName',
            df.columns[2]: 'State',
            df.columns[3]: 'LastChangeTimestamp',
            df.columns[4]: 'HostName',
            df.columns[5]: 'HardwareAddress',
            df.columns[6]: 'HardwareVendor'
        })
        return result

    def scan(self):
        logger.info(f"Scanner invoked for {self._src_path}")
        try:
            logger.info("Running scan...")
            self._run_scan()
            logger.info("Reading results")
            df = self._read_changes(self._src_path)
            logger.info("Mapping DNS names")
            df["DNSNames"] = df['IpAddress'].apply(self._map_dns_names)
            logger.info("Checking for open ports")
            df["OpenPorts"] = df['IpAddress'].apply(self._map_open_ports)
            logger.info("converting to json records")
            json_records = self._to_json(df)
            logger.info("converting to log entries")
            logdata = self._to_logging_payload(json_records)
            logger.info("Posting log data")
            self._send_logging_payload(logdata)
            logger.info("Converting to metric data")
            metric_data = self._to_metric_payload(json_records)
            logger.info("Posting metric data")
            self._send_metric_payload(metric_data)
        except FileNotFoundError:
            logger.error("Could not open event: ", self._src_path)

    @staticmethod
    def _to_json(df):
        return json.loads(df.to_json(orient='records'))

    @staticmethod
    def _to_logging_payload(json_records):
        streams = []
        creation_time = time.time_ns()
        for record in json_records:
            streams.append({
                "stream": {
                    "job": "network_scan"
                },
                "values": [
                    [creation_time, record]
                ]
            })
            for port in record["OpenPorts"]:
                entry = copy.deepcopy(port)
                entry["HardwareVendor"] = record["HardwareVendor"]
                entry["DNSNames"] = record["DNSNames"]
                entry["IpAddress"] = record["IpAddress"]
                streams.append({
                    "stream": {
                        "job": "port_scan"
                    },
                    "values": [
                        [creation_time, entry]
                    ]
                })
        return {
            "streams": streams
        }

    @staticmethod
    def _to_metric_payload(json_records):
        def _add_to_metric(d, k):
            if k not in d:
                d[k] = 0
            d[k] += 1
        points = []
        ports = {}
        points.append(Point("Nodes").field("total", len(json_records)))

        for record in json_records:
            points.append(
                Point("Nodes")
                .tag("DNSName", record["DNSNames"])
                .tag("IpAddress", record["IpAddress"])
                .field("uptime", 20)
            )
            points.append(
                Point("Nodes")
                .tag("DNSName", record["DNSNames"])
                .tag("IpAddress", record["IpAddress"])
                .field("active", True)
            )
            points.append(
                Point("Nodes")
                .tag("DNSName", record["DNSNames"])
                .tag("IpAddress", record["IpAddress"])
                .field("OpenPorts", len(record["OpenPorts"]))
            )

            for port in record["OpenPorts"]:
                points.append(
                    Point("Nodes")
                    .tag("DNSName", record["DNSNames"])
                    .tag("IpAddress", record["IpAddress"])
                    .field(port["name"], True).
                )
                _add_to_metric(ports, (port["protocol"], port["name"], port["port"]))
        for key in ports.keys():
            protocol, name, port = key
            points.append(
                Point("Ports")
                .tag("protocol", protocol)
                .tag("name", name)
                .tag("portnumber", port)
                .field("count", ports[key])
            )
        return points

    @staticmethod
    def _map_open_ports(ip_address):
        logger.debug(f"Started scanning on {ip_address}")
        stream = os.popen(f"/usr/bin/fing --servicescan {ip_address}/32  --silent -o csv")
        open_ports = []
        for line in stream.readlines():
            logger.debug(line)
            entries = line.split(";")
            open_ports.append({
                "port": entries[1],
                "protocol": entries[2],
                "name": entries[3]
            })
        return open_ports

    def _send_logging_payload(self, payload):
        url = f"http://{self._log_server}/loki/api/v1/push"
        headers = {
            'Content-type': 'application/json'
        }
        json_payload = json.dumps(payload)
        answer = requests.post(url, data=json_payload, headers=headers)
        try:
            logger.info(answer.json())
        except Exception:
            pass
        try:
            logger.info(answer.text)
        except Exception:
            pass

    def _send_metric_payload(self, metric_data):
        client = InfluxDBClient(
            url=f"http://{self._influx_server}",
            token=self._influx_token,
            org=self._influx_org
        )
        write_api = client.write_api(write_options=SYNCHRONOUS)

        for point in metric_data:
            write_api.write(
                bucket=self._influx_bucket,
                record=point
            )


@logger.catch
@click.command()
@click.option("-s", "--subnet")
@click.option("-f", "--log-file")
@click.option("-d", "--dns-server")
@click.option("-l", "--log-server")
@click.option("-i", "--influx-server")
@click.option("-t", "--influx-token")
@click.option("-o", "--influx-org")
@click.option("-b", "--influx-bucket")
def main(subnet, log_file, dns_server, log_server, influx_server, influx_token, influx_org, influx_bucket):
    logger.info("Monitor running")
    scanner = NetworkScanner(subnet, log_file, dns_server, log_server, influx_server, influx_token, influx_org, influx_bucket)
    logger.info("Scheduling every 20 mins")
    schedule.every(20).minutes.do(scanner.scan)
    logger.info("Starting initial run")
    scanner.scan()
    logger.info("Initial run completed")
    while True:
        schedule.run_pending()
        time.sleep(60)


if __name__ == "__main__":
    main()
