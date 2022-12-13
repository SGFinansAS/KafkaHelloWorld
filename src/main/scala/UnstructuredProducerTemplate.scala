import cats.effect.{IO, IOApp}
import fs2.{Pure, Stream}
import fs2.kafka._

object UnstructuredProducerTemplate extends IOApp.Simple {

  val topicName: String = ???
  val producerSettings: ProducerSettings[IO, Unit, String] = ???
  val records: Stream[IO, ProducerRecords[Unit, Unit, String]] = ???

  val run: IO[Unit] = {
    KafkaProducer
      .stream(producerSettings)
      .flatMap(
        producer => records
          .through(KafkaProducer.pipe(producerSettings, producer))
      )
      .compile
      .drain
  }
}
