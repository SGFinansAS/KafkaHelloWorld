package avro

import avro.domain.OnAccount
import avro.domain.OnAccount.onAccountSerializer
import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka._

object AvroProducerTemplate extends IOApp.Simple {

  private val topicName: String = ???
  private val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ???
  private val records: Stream[IO, ProducerRecords[Unit, Unit, OnAccount]] = ???

  val run: IO[Unit] =
    KafkaProducer
      .stream(producerSettings)
      .flatMap(
        producer => records
          .through(KafkaProducer.pipe(producerSettings, producer))
      )
      .compile
      .drain
}