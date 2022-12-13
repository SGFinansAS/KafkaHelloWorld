package avro.domain

import avro.schemaregistry.SchemaConfig.avroSettings
import cats.effect.IO
import cats.syntax.all._
import fs2.kafka.{RecordDeserializer, RecordSerializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer}
import vulcan.Codec

case class OnAccount(name: String,
                     number1: Int,
                     number2: Float)

object OnAccount {
  implicit val onAccountCodec: Codec[OnAccount] =
    Codec.record(
      name = "onAccount", // I had typo here : OnAccount, and got 404 from schemaRegistry
      namespace = "com.ff",
      doc = "onAccount2 value".some,
    ) { field =>
      (
        field("name", _.name),
        field("number1", _.number1),
        field("number2", _.number2)
        ).mapN(OnAccount(_, _, _))
    }

  implicit val onAccountSerializer: RecordSerializer[IO, OnAccount] =
    avroSerializer[OnAccount].using(avroSettings)

  implicit val onAccountDeserializer: RecordDeserializer[IO, OnAccount] =
    avroDeserializer[OnAccount]
      .using(avroSettings)

  /** this runs an http call to schemaRegistry with validation */

}