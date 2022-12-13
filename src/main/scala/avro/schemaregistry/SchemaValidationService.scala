package avro.schemaregistry

import avro.schemaregistry.SchemaConfig.schemaRegistryBaseUrl
import org.http4s.jdkhttpclient.JdkHttpClient
import cats.effect.IO
import fs2.io.file.{Files, Path}
import org.http4s.Request
import org.http4s._

object SchemaValidationService {

  private val localSchemaLocation: String = "src/main/scala/avro/domain"
  private val latestSchemaEndpoint: String = schemaRegistryBaseUrl + "/subjects/onAccount2-avro/versions/latest"

  private val currentVersionOfSchema: IO[Int] = Files[IO]
    .list(Path(localSchemaLocation))
    .map(p => p.toString.split("/").last)
    .filter(s => s.startsWith("onAccount2"))
    .filter(s => s.endsWith(".avsc"))
    .evalTap(s => IO.println(s"found the following avro file :\n\n\n $s\n\n\n"))
    .map(_
      .replace("onAccount2-avro-v", "")
      .replace(".avsc", "")
      .toInt
    )
    .compile
    .toList
    .map(_.head)


  def weAreUsingTheLatestSchema: IO[Boolean] = {
    val latestSchema: IO[Subject] = (
      for {
        client <- JdkHttpClient.simple[IO]
        response <- client.run(Request(Method.GET, Uri.unsafeFromString(latestSchemaEndpoint)))
      } yield response
      )
      .use {
        _.as[Subject]
      }

    for {
      ourCurrentVersion <- currentVersionOfSchema
      schema <- latestSchema
      _ <- IO.println(
        s"\n\nFound schema :\n\n$schema\n\n" +
          s"with version : ${schema.version}\n" +
          s"our version : $ourCurrentVersion\n" +
          s"equal : ${schema.version == ourCurrentVersion}\n\n")
    } yield schema.version == ourCurrentVersion
  }
}