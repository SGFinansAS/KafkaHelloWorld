package avro

import avro.domain.OnAccount
import avro.domain.OnAccount.onAccountSerializer
import avro.schemaregistry.SchemaValidationService
import cats.effect.{IO, IOApp}
import fs2.kafka._
import fs2.Stream

object AvroProducer extends IOApp.Simple {

  private val topicName = "onAccount3"
  private val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ProducerSettings( /** NEW */
    keySerializer = Serializer[IO, Unit],
    valueSerializer = onAccountSerializer
  ).withBootstrapServers("localhost:9092")
  private val message: Stream[IO, String] = Stream("just some text")

  private def publishMessage: IO[Unit] =
    KafkaProducer
      .stream(producerSettings)
      .flatMap(
        producer => message
          .map(msg => OnAccount(msg, 1, 1)) /** NEW */
          .evalTap(msg => IO.println(s"About to publish message: $msg"))
          .map(ProducerRecord(topicName, (), _))
          .evalTap(_ => IO.println("Successfully mapped to ProducerRecord"))
          .map(ProducerRecords.one(_))
          .evalTap(_ => IO.println("Successfully bundled to ProducerRecords"))
          .evalTap(_ => IO.println("about to publish..."))
          .through(KafkaProducer.pipe(producerSettings, producer))
          .evalTap(publishResult => IO.println(s"Successfully published msg: ${publishResult.records.head.get._1.value}"))
      )
      .compile
      .drain

  val run: IO[Unit] =
    for {
      weAreUsingTheLatestSchema <- IO.pure(true)//SchemaValidationService.weAreUsingTheLatestSchema
      _ <- if (weAreUsingTheLatestSchema) publishMessage
      else IO.println("Did not publish because we are not using the latest schema in SchemaRegistry.\n\n")
    } yield ()

}