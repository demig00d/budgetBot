package budget.bot

import cats.arrow.FunctionK
import cats.effect.{ ExitCode, IO, IOApp, Resource }
import cats.implicits.catsSyntaxApplicativeId

import doobie.hikari.HikariTransactor
import doobie.{ ConnectionIO, ExecutionContexts }
import doobie.implicits.toConnectionIOOps

import org.typelevel.log4cats.StructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import telegramium.bots.high.{ Api, BotApi }

import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.Logger

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

import handlers.{ MessageHandler, MessageHandlerImpl }
import models.CommandParser
import repositories.*
import services.*

object BotApplication extends IOApp {
  type F[+A] = IO[A]
  type G[A] = ConnectionIO[A]

  private val log: StructuredLogger[F] = Slf4jLogger.getLogger[F]

  override def run(args: List[String]): F[ExitCode] =
    for {
      config <- Config.load[F]
      _ <- log.info(s"Configuration is loaded: $config")
      _ <- resources(config).use {
        case (transactor, httpClient) =>
          assembleAndLaunch(
            config,
            transactor,
            httpClient,
            log,
          )
      }
    } yield ExitCode.Success

  def resources(
      config: Config
    ): Resource[F, (HikariTransactor[F], Client[F])] = for {
    httpCp <- ExecutionContexts.cachedThreadPool[F]
    tranEc <- ExecutionContexts.cachedThreadPool[F]
    transactor <- DB.createTransactor[F](config.database, tranEc)
    httpClient <- BlazeClientBuilder[F]
      .withExecutionContext(httpCp)
      .withResponseHeaderTimeout(FiniteDuration(25, TimeUnit.SECONDS))
      .resource
  } yield (transactor, httpClient)

  def assembleAndLaunch(
      config: Config,
      hikariTransactor: HikariTransactor[F],
      httpClient: Client[F],
      log: StructuredLogger[F],
    ): F[Unit] =

    given logger: StructuredLogger[F] = log

    given transactor: FunctionK[G, F] = new FunctionK[G, F] {
      override def apply[A](a: G[A]): F[A] =
        a.transact(hikariTransactor)
    }

    given budgetStateRepo: BudgetStateRepo[G] = BudgetStateRepoImpl
    given budgetStateService: BudgetStateService[F] = BudgetStateServiceImpl.make[F, G]

    val client = Logger(logHeaders = false, logBody = false)(httpClient)

    given api: BotApi[F] =
      BotApi[F](client, s"https://api.telegram.org/bot${config.tgBotToken}")

    val helpMessage = CommandParser.formHelpMessage
    given messageHandler: MessageHandler[F] = MessageHandlerImpl.make[F](helpMessage)

    val budgetBot = 
      Bot.make(baseUrl = config.webhook.url, path = config.tgBotToken)

    budgetBot.start(config.webhook.port, config.webhook.host).useForever.as(ExitCode.Success)

}
