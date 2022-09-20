package budget.bot
package models

import telegramium.bots.ChatIntId

import java.time.LocalDateTime

final case class BudgetState(
    chatId: ChatIntId,
    balance: Int,
    history: Option[String],
  ) {
  def add(
      amount: Int,
      transactionInfo: String,
    ): BudgetState =
    val operationDateTime =
      LocalDateTime
        .now
        .toString
        .take(16)
        .replace("-", ".")
        .replace("T", " ")

    val formattedTransactionInfo =
      transactionInfo
        .replace("/income ", "+")
        .replace("/expense ", " -")
        .replace("\n", s"\n[$operationDateTime] ")

    val oldHistory =
      history
        .map(_ :+ '\n')
        .getOrElse("")

    BudgetState(
      chatId = chatId,
      balance = balance + amount,
      history = Some(s"$oldHistory[$operationDateTime] $formattedTransactionInfo"),
    )

}

object BudgetState:
  def showBalance(balance: Int): String = s"Бюджет: $balance"

  def apply(
      chatId: ChatIntId,
      balance: Int,
      history: String,
    ): BudgetState =
    BudgetState(
      chatId = chatId,
      balance = balance,
      history = Some(history),
    )
