package avro.schemaregistry

import io.circe.derivation.deriveDecoder
import cats.effect.IO
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class Subject(subject: String,
                   version: Int,
                   id: Int,
                   schema: String)

object Subject {
  implicit val decoder: Decoder[Subject] = deriveDecoder
  implicit val entityDecoder: EntityDecoder[IO, Subject] = jsonOf
}
