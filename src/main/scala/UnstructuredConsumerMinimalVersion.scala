import cats.effect.{IO, IOApp}
import fs2.kafka._

object UnstructuredConsumerMinimalVersion extends IOApp.Simple {

  val topicName: String = "onAccount1"
  val consumerSettings: ConsumerSettings[IO, Unit, String] = ConsumerSettings[IO, Unit, String]
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers("localhost:9092")
    .withGroupId("groupId")

  val run: IO[Unit] = {
    KafkaConsumer.stream(consumerSettings)
      .subscribeTo(topicName) // listens indefinitely
      .records
      .evalTap(r => IO.println(s"read record : $r"))
      .compile.drain
  }
}