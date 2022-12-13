import cats.effect.{IO, IOApp}
import fs2.kafka._
import fs2.Stream

object UnstructuredProducer extends IOApp.Simple {
  val run: IO[Unit] = {

    val topicName = "onAccount1"

    val producerSettings = ProducerSettings[IO, Unit, String]
      .withBootstrapServers("localhost:9092")

    /**
     * Creating a stream of a messages to publish
     */

    val messages: Stream[IO, String] = Stream(
      "yay produced a message from code 400",
      "yay produced a message from code 500",
      "yay produced a message from code 600"
    )

    /**
     * Publishing the records
     */

    KafkaProducer
      .stream(producerSettings)
      .flatMap(
        producer => messages

          .evalTap(msg => IO.println(s"About to publish message: $msg"))

          .map(ProducerRecord(topicName, (), _))
          .map(ProducerRecords.one(_))
          .through(KafkaProducer.pipe(producerSettings, producer))

          .evalTap(result => IO.println(s"Successfully published msg: ${result.records.head.get._1.value}"))
      )
      .compile
      .drain
  }
}
