### Infra config in the docker-compose.yml

- 1 broker, 1 replica, 1 partition
- Zookeeper (manager of brokers & leaders)
- kafdrop (UI)
- rest-proxy (nice REST interface for some admin stuff)

# Sources
used to create this setup :

- https://www.baeldung.com/ops/kafka-docker-setup
- https://github.com/obsidiandynamics/kafdrop/blob/master/docker-compose/kafka-kafdrop/docker-compose.yaml
- https://github.com/confluentinc/demo-scene/blob/master/community-components-only/docker-compose.yml#L22-L51
- https://developer.confluent.io/quickstart/kafka-docker/

Sources didn't combine well out of the box, lil bit debugging was needed.

In general, how to use kafka without kafka-rest
- https://kafka.apache.org/quickstart
- https://developer.confluent.io/quickstart/kafka-docker/

How to use kafka-rest :
- https://hub.docker.com/r/dockerkafka/confluent-kafka-rest
- https://docs.confluent.io/platform/current/kafka-rest/quickstart.html#produce-and-consume-json-messages
Supported content-types:
- https://docs.confluent.io/platform/current/kafka-rest/api.html#content-types

Using fs2-kafka
- https://fd4s.github.io/fs2-kafka/docs/producers
- https://fs2.io/#/guide?id=building-streams

Schema-registry API
- curls @ `https://github.com/confluentinc/schema-registry/blob/master/README.md`
- general api @ `https://docs.confluent.io/platform/current/schema-registry/develop`
- distributed linking @ `https://docs.confluent.io/platform/current/schema-registry/schema-linking-cp.html`

Schema-registry UI
- https://github.com/lensesio/schema-registry-ui
- https://hub.docker.com/r/landoop/schema-registry-ui/




### In scope : 

- Creating and deleting topics
- Publishing messages from commandline
- Creating consumer groups and consuming messages from commandline
- Using UIs

- Publishing messages from code
- Creating consumer groups and consuming messages from code
- Avro schemas

### Outside scope :

Infra : 
- redundancy (partitions & topics)
- message-keys
- ACLs
- security practices (https://docs.confluent.io/platform/current/schema-registry/security/index.html#sr-security-overview)
- RAFT : Replacing Zookeeper leader election with Kafka leader election

Complex logic
- manual committing
- passthrough values

Schemas : 
- nested schemas
- generic records for schemas (runtime fetching & with shapeless)
- Broker-side schema validation (Confluent Server/Platform)

Additional kafka-tech
- Kafka Connect
- Kafka In the Cloud (10x better)
- Apache vs Confluent

.. and much more

### Part 1 : First spin up the local infra

`$ docker-compose up -d`


### Part 2 : Playing with the command line and browser

Visit the UI : `http://localhost:9000`

List topics with `curl http://localhost:8082/topics`

Create topic "onAccount1" from UI @ `http://localhost:9000`

Create topic "onAccount2" internally using
`docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic onAccount2`


Create topic "onAccount3" using rest-proxy
- First find out the cluster id `curl http://localhost:8082/v3/clusters`, search for something in the response like `{"cluster_id":"PhzR5OojSimwIOxl_ANQ5g"}`


Create topics at `http://localhost:8082/v3/clusters/{cluster_id}/topics` :
```
curl -X POST http://localhost:8082/v3/clusters/PhzR5OojSimwIOxl_ANQ5g/topics \
  --header 'Content-Type: application/vnd.api+json' \
  --data-raw '{
          "data": {
               "attributes": {
                    "topic_name": "onAccount3",
                    "partitions_count": 1,
                    "replication_factor": 1
               }
          }
     }'
```


Delete topic :
```
curl -X DELETE http://localhost:8082/v3/clusters/PhzR5OojSimwIOxl_ANQ5g/topics/onAccount4
```

Write a JSON msg to topic with no schema :
```
curl -X POST http://localhost:8082/topics/onAccount1 \
  -H "Content-Type: application/vnd.kafka.json.v2+json" \
  --data '{"records":[{"value": {"id": "some-value"}}]}'
```

Get topic information :
```curl http://localhost:8082/topics/onAccount1```

Create a consumer group to consume : /consumer/{consumerGroup} with 'name: consumerId'
```
curl -X POST http://localhost:8082/consumers/workflow \
   --header "Content-Type: application/vnd.kafka.json.v2+json" \
   --data '{"name": "claudio", "format": "json", "auto.offset.reset": "earliest"}'
```

Subscribe to a topic : /consumers/{group_id}/instances/{consumer_id}/subscription
```
curl -X POST http://localhost:8082/consumers/workflow/instances/claudio/subscription \
  -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["onAccount1"]}'
```

Consume all records :
```
curl http://localhost:8082/consumers/workflow/instances/claudio/records \
  -H "Accept: application/vnd.kafka.json.v2+json"
```



Produce an AVRO msg
```
curl -v http://localhost:8082/topics/onAccount1 \
 -H "Content-Type: application/vnd.kafka.avro.v2+json" \
 -H "Accept: application/vnd.kafka.v2+json" \
 --data '{"value_schema": "{\"type\": \"record\", \"name\": \"User\", \"fields\": [{\"name\": \"name\", \"type\": \"string\"}]}", "records": [{"value": {"name": "testUser"}}]}'
```

fails with msg :  `{"error_code":40801,"message":"Schema registration or lookup failed"}[`
because we are not running with schema-registry


## Part 3 : Playing with code

1. Produce some stuff
2. Consume some stuff
3. Play with AutoOffsetReset
4. Admin some stuff


## Part 4 : schemas 

UI @ `http://localhost:8000`

Schema-registry Endpoints :
- `curl http://localhost:8081/config`                                  see current config
- `curl http://localhost:8081/subjects`                                see existing schema names
- `curl http://localhost:8081/subjects/{subject}/versions`             see how many versions of a schema
- `curl http://localhost:8081/subjects/{subject}/versions/latest`             get latest version of a schema
- `curl http://localhost:8081/schemas`                                 see existing schemas with details
- `curl http://localhost:8081/schemas/ids/{int}`                       see specific schema



1. Create a schema for a topic using naming convention `topic-format`
```
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{...some-json...}' \
http://localhost:8081/subjects/{topic-format}/versions
```
- Delete a schema

Using schema in code
1. Download the schema
2. Change the postfix to `.avsc`
3. Use it in producing & consuming

Kafka Vulcan Producer does calls (visible with `docker logs -f schema-registry`)


# Post-Discussion

Vulcan library :
- by default does strong validations : overrides available or go to complete custom logic if necessary ?

Confluent Platform : something to look into someday ?
- https://docs.confluent.io/platform/current/platform-quickstart.html