package budget.bot
package services

import cats.Monad
import cats.arrow.FunctionK
import cats.syntax.all.*
import telegramium.bots.ChatIntId

import models.BudgetState
import repositories.*
import models.*

trait BudgetStateService[F[_]]:
  def add(chatId: ChatIntId): F[Unit]
  def getBudgetState(chatId: ChatIntId): F[Option[BudgetState]]
  def getHistory(chatId: ChatIntId): F[String]
  def getBalance(chatId: ChatIntId): F[Int]
  def updateBalanceAndHistory(
      chatId: ChatIntId,
      amount: Int,
      transactionInfo: String,
    ): F[Int]

object BudgetStateServiceImpl:
  def make[F[_]: Monad, G[_]: Monad](
      using
      transactor: FunctionK[G, F],
      budgetStateRepo: BudgetStateRepo[G],
    ): BudgetStateService[F] =
    new BudgetStateService[F]:
      def add(chatId: ChatIntId): F[Unit] =
        transactor(budgetStateRepo.insert(chatId))

      def getBudgetState(chatId: ChatIntId): F[Option[BudgetState]] =
        transactor(budgetStateRepo.select(chatId))

      def getHistory(chatId: ChatIntId): F[String] =
        getBudgetState(chatId).map { maybeBudgetState =>
          maybeBudgetState
            .map(_.history)
            .flatten
            .getOrElse("История появится после первой команды учёта")
        }

      def getBalance(chatId: ChatIntId): F[Int] =
        getBudgetState(chatId).map { maybeBudgetState =>
          maybeBudgetState
            .map(_.balance)
            .getOrElse(0)
        }

      def updateBalanceAndHistory(
          chatId: ChatIntId,
          amount: Int,
          transactionInfo: String,
        ): F[Int] =
        transactor(
          for {
            maybeBudgetState <- budgetStateRepo.select(chatId)
            budget = maybeBudgetState match
              case Some(budgetState) => budgetState.add(amount, transactionInfo)
              case None => BudgetState(chatId, amount, transactionInfo)
            _ <- budgetStateRepo.update(budget)
          } yield budget.balance
        )
