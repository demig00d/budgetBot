package budget.bot
package repositories

import cats.implicits.*
import doobie.implicits.*
import doobie.util.{ Read, Write }
import doobie.{ ConnectionIO, LogHandler }
import telegramium.bots.ChatIntId

import models.BudgetState

trait BudgetStateRepo[F[_]]:
  def insert(chatId: ChatIntId): F[Unit]
  def update(budgetState: BudgetState): F[Unit]
  def select(chatId: ChatIntId): F[Option[BudgetState]]

object BudgetStateRepoImpl extends BudgetStateRepo[ConnectionIO]:
  given logHandler: LogHandler = LogHandler.jdkLogHandler

  given chatIdWrite: Write[ChatIntId] =
    Write[Long].contramap(_.id)

  given budgetStateRead: Read[BudgetState] =
    Read[(Long, Int, Option[String])].map {
      case (chatId, balance, history) => BudgetState(ChatIntId(chatId), balance, history)
    }

  def insert(chatId: ChatIntId): ConnectionIO[Unit] =
    sql"""INSERT INTO budget_state (chat_id)
          VALUES ($chatId)
          ON CONFLICT DO NOTHING""".update.run.void

  def update(budgetState: BudgetState): ConnectionIO[Unit] =
    import budgetState.*
    sql"""UPDATE budget_state 
            SET balance = $balance,
                history = $history
          WHERE chat_id = $chatId""".update.run.void

  def select(chatId: ChatIntId): ConnectionIO[Option[BudgetState]] =
    sql"""SELECT * FROM budget_state
          WHERE chat_id = $chatId""".query[BudgetState].option
