package avro

import avro.domain.OnAccount
import cats.effect.{IO, IOApp}
import fs2.kafka._
import avro.domain.OnAccount.onAccountDeserializer

object AvroConsumerMinimalVersion extends IOApp.Simple {

  val topicName: String = "onAccount2"  // used in schema-validation
  val consumerSettings: ConsumerSettings[IO, Unit, OnAccount] = ConsumerSettings(
    keyDeserializer = Deserializer[IO, Unit],
    valueDeserializer = onAccountDeserializer // validates schemaRegistry because we use .using(avroSettings)
  )
    .withAutoOffsetReset(AutoOffsetReset.Earliest) // where to start reading for the first time
    .withBootstrapServers("localhost:9092")
    .withGroupId("groupId")
    // .withEnableAutoCommit(true) WHAT DOES THIS DO ? see UI

  val run: IO[Unit] = {
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(topicName) // listens indefinitely
      .records // validates each individual msg against living schemas in schema registry, problems ?
      .evalTap(r => IO.println(s"read record : $r"))
      //.map(_ => throw new RuntimeException("oops")) // what happens now ?
      .compile.drain
  }
}
