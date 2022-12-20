package avro

import avro.OnAccountPublisherConfig.topicName
import cats.effect.syntax.all._
import avro.domain.OnAccount
import avro.domain.OnAccount._
import cats.Monad
import cats.effect.std.Console
import cats.effect.{IO, IOApp}
import fs2.kafka._

object Main extends IOApp.Simple {

  private def askName = for {
    _ <- Console[IO].println("Hi, please enter a name for the message: ")
    name <- Console[IO].readLine
  } yield name

  def askNamePublishRepeat(publisher: OnAccountPublisher): IO[Unit] = {
    Monad[IO].whileM_(IO.pure(true)) {
      for {
        name <- askName
        _ <- publisher.safePublish(OnAccount(name, 1, 1))
      } yield ()
    }
  }

  val run: IO[Unit] = {
    val program = for {
      producer <- KafkaProducer.resource(OnAccountPublisherConfig.producerSettings)
      _ <- askNamePublishRepeat(new OnAccountPublisher(producer)).toResource
    } yield ()

    program.useForever
  }
}

trait KafkaPublisher {
  def safePublish(msg: OnAccount): IO[Unit]
}

class OnAccountPublisher(publisher: KafkaProducer[IO, Unit, OnAccount]) extends KafkaPublisher {

  override def safePublish(onAccount: OnAccount): IO[Unit] = for {
    publishResult <- publish(onAccount).attempt
    _ <- publishResult match {
      case Left(error) => IO.println(error)
      case _ => IO.unit
    }
  } yield ()

  private def publish(onAccount: OnAccount): IO[Unit] = {
    if (onAccount.name == "error")
      IO.raiseError(new Exception("error name"))
    else
      publisher.produce(ProducerRecords.one(ProducerRecord(topicName, (), onAccount)))
        .flatten
        .flatMap(r => IO.println(s"published record : $r"))
  }
}

object OnAccountPublisherConfig {
  val topicName: String = "onAccount1"
  val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ProducerSettings(
    keySerializer = Serializer[IO, Unit],
    valueSerializer = onAccountSerializer
  ).withBootstrapServers("localhost:9092")
}






