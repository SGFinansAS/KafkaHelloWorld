import cats.effect._
import fs2.kafka._
import org.apache.kafka.clients.admin.{NewTopic, TopicDescription}

object KafkaAdmin extends IOApp.Simple {

  val run: IO[Unit] = {

    def kafkaAdminClientResource[F[_] : Async](
                                                bootstrapServers: String
                                              ): Resource[F, KafkaAdminClient[F]] =
      KafkaAdminClient.resource[F](AdminClientSettings(bootstrapServers))


    val prettyPrintDescriptions: Map[String, TopicDescription] => String =
      _.map(entry => s"${entry._1}\n${entry._2}\n\n")
        .foldLeft("")((acc, s) => acc + s)

    kafkaAdminClientResource[IO]("localhost:9092").use {
      adminClient =>
        for {
          //_ <- client.createTopic(new NewTopic("topic-created-with-code", 1, 1.toShort))
          topicNames <- adminClient.listTopics.names
          descriptions <- adminClient.describeTopics(topicNames.toList)
          _ <- IO.println(prettyPrintDescriptions(descriptions))
        } yield ()
    }
  }
}
