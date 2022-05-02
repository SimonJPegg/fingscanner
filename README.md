# fingscanner

Docker container that uses the [Fing](https://www.fing.com/) CLI to monitor a network and load the results
into an InfluxDB instance as metrics and a loki server as Json logs

Takes the following env vars: 

* SUBNET: The subnet to scan
* INFLUX_SERVER: The URL of the influx DB instance
* INFLUX_TOKEN: The security token to use with the instance
* INFLUX_ORG: The influx organisation 
* INFLUX_BUCKET:The bucket to store the data
* DNS_SERVER: A DNS server to use for hostname lookups (e.g. inside a docker swarm).  Defaults to 8.8.8.8
* LOG_SERVER: The URL of the Loki server
* ACTOR_POOL: The size of the actor pools inside the container (not required)
* SCHEDULE: The schedule to scan on in minutes

N.B. this has been created for personal use and probably doesn't comply with Fing's usage policy for their CLI.
