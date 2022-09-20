package budget.bot
package handlers

import cats.Monad
import cats.implicits.*
import telegramium.bots.{ ChatIntId, Message, User, CallbackQuery }

import services.BudgetStateService
import models.{ Action, BudgetState, CommandParser, Reaction }
import models.Reaction.*

trait MessageHandler[F[_]]:
  def handle(chatId: ChatIntId, text: String): F[Reaction]

object MessageHandlerImpl:
  def make[F[_]: Monad](
      helpMessage: String
    )(using
      budgetStateService: BudgetStateService[F]
    ): MessageHandler[F] =
    new MessageHandler[F]:
      override def handle(
          chatId: ChatIntId,
          text: String,
        ): F[Reaction] =
        CommandParser.toAction(text) match

          case Action.ShowGreeting =>
            budgetStateService.add(chatId) >>
              SendText(
                chatId,
                """|Приветствую,
                   |я бот для групповых чатов с функцией введения бюджета, 
                   |
                   |введите комманду /help чтобы посмотреть подробности""".stripMargin,
              ).pure[F]

          case Action.ShowHelp =>
            SendText(
              chatId,
              s"""|Вот что я могу:
                  | 
                  |$helpMessage""".stripMargin,
            ).pure[F]

          case Action.ExecIncome(amount, transactionInfo) =>
            budgetStateService
              .updateBalanceAndHistory(
                chatId,
                amount,
                transactionInfo,
              )
              .map { newBalance =>
                SendText(chatId, BudgetState.showBalance(newBalance))
              }

          case Action.ExecExpense(amount, transactionInfo) =>
            budgetStateService
              .updateBalanceAndHistory(
                chatId,
                if amount < 0 then amount else -amount,
                transactionInfo,
              )
              .map { newBalance =>
                SendText(chatId, BudgetState.showBalance(newBalance))
              }

          case Action.CalcListOfOperations(amountsTransactionInfos) =>
            val (amount, transactionInfo) =
              amountsTransactionInfos.foldLeft(0, "") {
                case ((a1, ti1), (a2, ti2)) => (a1 + a2, s"$ti1\n$ti2")
              }

            budgetStateService
              .updateBalanceAndHistory(
                chatId,
                amount,
                transactionInfo.tail,
              )
              .map { newBalance =>
                SendText(chatId, BudgetState.showBalance(newBalance))
              }

          case Action.ShowHistory =>
            budgetStateService.getBudgetState(chatId).map { maybeBudgetState =>
              val history = maybeBudgetState
                .map(_.history)
                .flatten
                .getOrElse("История появится после первой команды учёта")
              SendText(chatId, history)
            }
          case Action.ShowBudget =>
            budgetStateService.getBalance(chatId).map { balance =>
              SendText(chatId, BudgetState.showBalance(balance))
            }
          case Action.ClaimWrongInput(text) => SendText(chatId, text).pure[F]
          case Action.Ignore => Reaction.DoNothing.pure[F]
