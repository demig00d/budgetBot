package budget.bot

import cats.effect.kernel.Sync
import pureconfig.ConfigReader.Result
import pureconfig.generic.derivation.ConfigReaderDerivation.Default.derived
import pureconfig.{ ConfigReader, ConfigSource }

import Config.{DatabaseConfig, Webhook}

case class Config(
    database: DatabaseConfig,
    webhook: Webhook,
    tgBotToken: String,
  ) derives ConfigReader

object Config:
  def load[F[_]: Sync]: F[Config] =
    Sync[F].blocking(ConfigSource.default.loadOrThrow[Config])

  case class Webhook(
      url: String,
      port: Int,
      host: String
    )

  case class DatabaseConfig(
      driver: String,
      url: String,
      user: String,
      password: String,
    )
