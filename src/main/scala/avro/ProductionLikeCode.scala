package avro

import avro.OnAccountPublisherConfig.{producerSettings, topicName}
import cats.effect.syntax.all._
import avro.domain.OnAccount
import avro.domain.OnAccount._
import cats.Monad
import cats.effect.std.Console
import cats.effect.{IO, IOApp}
import fs2.kafka._

/**
 * Runs a commandline app that loops the following : ask for a name and publish it inside an AVRO message.
 *
 * - Enter the name "error" to test error-handling
 * - Other names get published
 *
 * */
object Main extends IOApp.Simple {

  private def askNamePublishRepeat(publisher: OnAccountPublisher): IO[Unit] = {
    Monad[IO].whileM_(IO.pure(true)) {
      for {
        _ <- Console[IO].println("Hi, please enter a name for the message: ")
        name <- Console[IO].readLine
        _ <- publisher.safePublish(OnAccount(name, 1, 1))
      } yield ()
    }
  }

  val run: IO[Unit] = {
    val program = for {
      producer <- KafkaProducer.resource(producerSettings)
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
      publisher.produceOne_(ProducerRecord(topicName, (), onAccount))
        .flatten
        .flatMap(md => IO.println(s"published record : $onAccount with offset ${md.offset()}"))
  }
}

object OnAccountPublisherConfig {
  val topicName: String = "onAccount1"
  val producerSettings: ProducerSettings[IO, Unit, OnAccount] = ProducerSettings(
    keySerializer = Serializer[IO, Unit],
    valueSerializer = onAccountSerializer
  ).withBootstrapServers("localhost:9092")
}






