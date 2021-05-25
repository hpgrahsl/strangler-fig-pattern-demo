# Strangler Fig Pattern Demo

## Build applications

Before being able to spin up the docker-compose based demo environment please make sure to successfully **build all 3 projects - the petclinic monolith based on Spring, the Apache Kafka Streams application and the owner microservice powered by Quarkus:**

```
./build-applications.sh
```

## Run with Docker

Spin up the demo environment by means of Docker compose:

```
docker-compose -f docker-compose-all.yaml up
```

## Strangler Fig Proxy

This demo uses nginx and depending on the request, routes either towards petclinic monolith or the owner microservice.

1. The proxy serves a static index page at `http://localhost` just to verify it's up and running.

2. The initial proxy configuration - see `docker/.config/nginx/nginx_initial.conf` - routes all requests starting with `http://localhost/petclinic` to the monolithic application.

3. As a first adaption, the proxy is reconfigured - see `docker/.config/nginx/nginx_read.conf` - to route a specific read request i.e. the owner search from the monolith to the corresponding microservice.

_NOTE: In order for this to work the CDC setup (from monolith -> microservice) needs to be configured properly based on Apache Kafka Connect (see below)_

4. As a second adaption, the proxy is reconfigured - see `docker/.config/nginx/nginx_initial.conf` - to route owner edit requests to the owner microservice as well.

_NOTE: In order for this to work the CDC setup (from microservice -> monolith) needs to be configured properly based on Apache Kafka Connect (see below)_

## Proxy Reconfiguration

With the docker compose stack up and running, the proxy can be reconfigured by running the following simple script with one of the 3 support parameters:

```
./proxy_config.sh initial | read | read_write
```

## Apache Kafka Connect Setup for CDC Pipelines

### CDC from monolith (MySQL) -> microservice (MongoDB)

1. Create MySQL source connector for owners + pets tables:

```
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mysql-source-owners-pets.json
```

2. Create MongoDB sink connector for pre-joined owner with pets aggregates:

```
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mongodb-sink-owners-pets.json
```

## Apache Kafka Connect Setup for CDC Pipelines

### CDC from microservice (MongoDB) -> monolith (MySQL)

1. Before processing writes at the microservice in MongoDB remove the MySQL source connector from the monolith database

```
curl -i -X DELETE -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/petclinic-owners-pets-mysql-src-001
```

2. Create MongoDB source connector  to capture data changes in the microservice

```
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mongodb-source-owners.json
```

3. Create MySQL JDBC sink connector  to propagate changes from the microservice into the monolith's database

```
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-jdbc-mysql-sink-owners.json
```

## Consume messages from CDC-related Apache Kafka topics

```
docker-compose -f docker-compose-all.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic mysql1.petclinic.owners
```

```
docker-compose -f docker-compose-all.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic mysql1.petclinic.pets
```

```
docker-compose -f docker-compose-all.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic kstreams.pets-with-visits
 ```

 ```
docker-compose -f docker-compose-all.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic mongodb.petclinic.kstreams.owners-with-pets
```
