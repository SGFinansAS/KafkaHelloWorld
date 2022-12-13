package avro

import avro.domain.OnAccount
import avro.domain.OnAccount._
import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka._

object AvroProducerMinimalVersion extends IOApp.Simple {

  private val topicName: String = "onAccount2" // used in schema-validation
  private val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ProducerSettings(
    keySerializer = Serializer[IO, Unit],
    valueSerializer = onAccountSerializer // will look for schema of form "onAccount3-value", because we use ".using(avroSettings)
  ).withBootstrapServers("localhost:9092")
  private val records: Stream[IO, ProducerRecords[Unit, Unit, OnAccount]] = Stream(ProducerRecords.one(ProducerRecord(topicName, (), OnAccount("", 1, 1))))

  val run: IO[Unit] =
    KafkaProducer
      .stream(producerSettings)
      .flatMap(
        producer => records
          .through(KafkaProducer.pipe(producerSettings, producer))
          .evalTap(r => IO.println(s"published record : $r"))
      )
      .compile
      .drain

}