package avro

import avro.domain.OnAccount
import avro.domain.OnAccount.onAccountDeserializer
import avro.schemaregistry.SchemaValidationService
import cats.effect.{IO, IOApp}
import fs2.kafka._

object AvroConsumer extends IOApp.Simple {

  def readMessages: IO[Unit] = {

    /**
     * Will check for each message in topic if the schema is still supported
     *
     * If a schema is added later (or old schemas are deleted), reading those messages fails
     * Then, either do custom logic (override the validation) or delete the msges for
     * */
    val topicName = "onAccount3"

    val consumerSettings: ConsumerSettings[IO, Unit, OnAccount] =
      ConsumerSettings( /** NEW */
        keyDeserializer = Deserializer[IO, Unit],
        valueDeserializer = onAccountDeserializer
      )
        .withAutoOffsetReset(AutoOffsetReset.Earliest) // where to start reading for the first time
        .withBootstrapServers("localhost:9092")
        .withGroupId("groupId")

    KafkaConsumer
      .stream(consumerSettings)
      .evalTap(_ => IO.println("About to subscribe..."))
      .subscribeTo(topicName)
      .evalTap(_ => IO.println("Subscribed.\nAbout to fetch records..."))
      .records
      .evalTap(_ => IO.println("Fetched records.\n\n"))
      .evalTap(r => {
        IO.println(s"consumed message with offset: ${r.offset.offsets.values.head.offset()} and value: ${r.record.value}")
      })
      .compile
      .drain
  }

  val run: IO[Unit] =
    for {
      weAreUsingTheLatestSchema <- IO.pure(true)//SchemaValidationService.weAreUsingTheLatestSchema
      _ <- if (weAreUsingTheLatestSchema) readMessages
      else IO.println("Did not read because we are not using the latest schema in SchemaRegistry.\n\n")
    } yield ()
}
