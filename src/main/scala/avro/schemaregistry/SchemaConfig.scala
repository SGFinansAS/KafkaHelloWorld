package avro.schemaregistry

import cats.effect.IO
import fs2.kafka.vulcan.{AvroSettings, SchemaRegistryClientSettings}

object SchemaConfig {

  val schemaRegistryBaseUrl: String = "http://localhost:8081"

  val avroSettings: AvroSettings[IO] =
    AvroSettings {
      SchemaRegistryClientSettings[IO](schemaRegistryBaseUrl)
      // .withAuth(Auth.Basic("username", "password"))
    }
      .withAutoRegisterSchemas(false) /** what happens if we uncomment this ? */
}
