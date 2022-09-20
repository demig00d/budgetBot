package budget.bot
package models


enum Command(val commandText: String, val description: String):
  case Start   extends Command("/start", "запустить бота")
  case Help    extends Command("/help", "показать эту справку")
  case History extends Command("/history", "показать историю операций")
  case Budget  extends Command("/budget", "показать бюджет")

enum TransactionCommand(val commandStartText: String, val description: String):
  case Income  extends TransactionCommand("/income ", "добавить сумму к бюджету")
  case Expense extends TransactionCommand("/expense ", "удалить сумму из бюджета")
  case Parse   extends TransactionCommand ("/calc\n", "считывает список комманд начиная со следующей строки")


object CommandParser:
  def toAction(text: String): Action =
    parse(text) match 
      case Command.Start              => Action.ShowGreeting
      case Command.Help               => Action.ShowHelp
      case Command.History            => Action.ShowHistory
      case Command.Budget             => Action.ShowBudget
      case TransactionCommand.Income  => Action.fromTransactionCommand(text)
      case TransactionCommand.Expense => Action.fromTransactionCommand(text)
      case TransactionCommand.Parse   => Action.fromParse(text)
      case None                       => Action.Ignore

  def parse(text: String) =
    parseCommand(text)
      .orElse(parseTransactionCommand(text))
      .getOrElse(None)

  private def parseCommand(text: String): Option[Command] = 
    Command.values.find{ (c: Command) =>
      text.toLowerCase.startsWith(c.commandText)
    }

  private def parseTransactionCommand(text: String): Option[TransactionCommand] =
    TransactionCommand.values.find{ (cfc: TransactionCommand) => 
      text.toLowerCase.startsWith(cfc.commandStartText)
    }

  def formHelpMessage: String = 
    Command
      .values
      .map((c) => s"${c.commandText} - ${c.description}") 
      .mkString("\n") ++ "\n" ++
    TransactionCommand
      .values
      .map((tc) => s"${tc.commandStartText} - ${tc.description}")
      .mkString("\n")

