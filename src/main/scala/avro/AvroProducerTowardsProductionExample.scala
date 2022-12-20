package avro

import cats.effect.syntax.all._
import avro.domain.OnAccount
import avro.domain.OnAccount._
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka._

object AvroProducerTowardsProductionExample extends IOApp.Simple {

  private val topicName: String = "onAccount1" // used in schema-validation
  private val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ProducerSettings(
    keySerializer = Serializer[IO, Unit],
    valueSerializer = onAccountSerializer // will look for schema of form "onAccount3-value", because we use ".using(avroSettings)
  ).withBootstrapServers("localhost:9092")

  trait Business {
    def publish(msg: OnAccount): IO[Unit]
  }

  val run: IO[Unit] = {

    val program = for {
      producingQueue <- Queue.bounded[IO, OnAccount](1).toResource
      _ <- KafkaProducer
        .stream(producerSettings)
        .flatMap(
          producer => Stream.fromQueueUnterminated(producingQueue)
            .debug()
            .evalTap(_ => IO.raiseError(new Exception()))
            .map(a =>
              ProducerRecords.one(ProducerRecord(topicName, (), a))
            )
            .through(KafkaProducer.pipe(producerSettings, producer))
            .evalTap(r => IO.println(s"published record : $r"))
        )
        .compile
        .resource
        .lastOrError

      businessProgram = new Business {
        override def publish(msg: OnAccount): IO[Unit] = producingQueue.offer(msg)
      }

      _ <- businessProgram.publish(OnAccount("", 1, 1)).toResource
    } yield ()

    program.useForever
  }
}