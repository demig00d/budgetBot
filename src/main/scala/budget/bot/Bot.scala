package budget.bot

import cats.Parallel
import cats.effect.{ Async, IO, Temporal }
import cats.implicits.*

import org.typelevel.log4cats.StructuredLogger

import telegramium.bots.{ ChatIntId, Message, User, CallbackQuery }
import telegramium.bots.high.{ Api, WebhookBot, Methods }
import telegramium.bots.high.implicits.methodOps

import handlers.MessageHandler
import models.*

object Bot:
  def make(
      baseUrl: String,
      path: String,
    )(
      using
      api: Api[IO],
      messageHandler: MessageHandler[IO],
      log: StructuredLogger[IO],
    ) =
    new WebhookBot[IO](api, s"$baseUrl/$path", path):
      override def onMessage(message: Message): IO[Unit] =
        message
          .text
          .map { text =>
            val chatId = ChatIntId(message.chat.id)
            messageHandler
              .handle(chatId, text)
              .recoverWith { th =>
                log
                  .info(s"Something went wrong $th")
                  .as(Reaction.DoNothing)
              }
              .flatMap(interpret)
          }
          .getOrElse(IO.unit)
          .start
          .void

      private def interpret(reaction: Reaction): IO[Unit] =
        reaction match {
          case Reaction.SendText(chatId, text, replyMarkup, parseMode) =>
            log.info(s"Sending message $text") >>
              Methods
                .sendMessage(
                  chatId = chatId,
                  text = text,
                  replyMarkup = replyMarkup,
                  parseMode = parseMode,
                )
                .exec
                .void

          case Reaction.DoNothing => IO.unit

        }
