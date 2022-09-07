package budget.bot

import cats.effect.{ Async, Resource, Sync }
import doobie.hikari.HikariTransactor
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

import Config.DatabaseConfig

object DB:
  def createTransactor[F[_]: Async](
      config: DatabaseConfig,
      ec: ExecutionContext,
    ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      driverClassName = config.driver,
      url = config.url,
      user = config.user,
      pass = config.password,
      connectEC = ec,
    )
