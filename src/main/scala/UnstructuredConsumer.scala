import cats.effect.{IO, IOApp}
import fs2.kafka._

object UnstructuredConsumer extends IOApp.Simple {
  val run: IO[Unit] = {

    val topicName = "onAccount1"

    val consumerSettings =
      ConsumerSettings[IO, Unit, String] // make sure this matches the types of producing
        .withAutoOffsetReset(AutoOffsetReset.Earliest) // where to start reading for the first time
        .withBootstrapServers("localhost:9092")
        .withGroupId("groupId")

    val stream =
      KafkaConsumer.stream(consumerSettings)
        .subscribeTo(topicName)                     // listens indefinitely
        .records
        .evalTap(r => {
          IO.println(s"consumed message with offset: ${r.offset.offsets.values.head.offset()} and value: ${r.record.value}")
        })

    stream.compile.drain
  }
}