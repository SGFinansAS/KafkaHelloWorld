import cats.effect.{IO, IOApp}
import fs2.kafka._

object UnstructuredConsumerTemplate extends IOApp.Simple {

  val topicName: String = ???
  val consumerSettings: ConsumerSettings[IO, Unit, String] = ???

  val run: IO[Unit] = {
      KafkaConsumer.stream(consumerSettings)
        .subscribeTo(topicName) // listens indefinitely
        .records
        .compile.drain
  }
}