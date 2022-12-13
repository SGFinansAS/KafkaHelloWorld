import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka._

object UnstructuredProducerMinimalVersion extends IOApp.Simple {
  val run: IO[Unit] = {

    val topicName: String = "onAccount1" // org.apache.kafka.common.errors.TimeoutException: Topic topic-3 not present in metadata after 60000 ms.
    val producerSettings: ProducerSettings[IO, Unit, String] = ProducerSettings[IO, Unit, String].withBootstrapServers("localhost:9092")
    val records: Stream[IO, ProducerRecords[Unit, Unit, String]] = Stream(ProducerRecords.one(ProducerRecord(topicName, (), "some-message-2")))

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
}
