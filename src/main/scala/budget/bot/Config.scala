package budget.bot

import Config.DatabaseConfig

import cats.effect.kernel.Sync
import pureconfig.ConfigReader.Result
import pureconfig.generic.derivation.ConfigReaderDerivation.Default.derived
import pureconfig.{ ConfigReader, ConfigSource }

case class Config(
    database: DatabaseConfig,
    tgBotToken: String,
  ) derives ConfigReader

object Config:
  def load[F[_]: Sync]: F[Config] =
    Sync[F].blocking(ConfigSource.default.loadOrThrow[Config])

  case class DatabaseConfig(
      driver: String,
      url: String,
      user: String,
      password: String,
    )
