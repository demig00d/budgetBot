package budget.bot
package models

import telegramium.bots.{ ChatIntId, ParseMode, KeyboardMarkup }

enum Reaction:
  case SendText(
      chatId: ChatIntId,
      text: String,
      replyMarkup: Option[KeyboardMarkup] = None,
      parseMode: Option[ParseMode] = None,
    ) extends Reaction

  case DoNothing extends Reaction
