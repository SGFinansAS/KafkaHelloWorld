package avro

import avro.domain.OnAccount
import cats.effect.{IO, IOApp}
import fs2.kafka._

object AvroConsumerTemplate extends IOApp.Simple {

  val topicName: String = ???
  val consumerSettings: ConsumerSettings[IO, Unit, OnAccount] = ???

  val run: IO[Unit] = {
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(topicName) // listens indefinitely
      .records
      .compile.drain
  }
}
