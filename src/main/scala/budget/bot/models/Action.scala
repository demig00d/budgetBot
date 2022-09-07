package budget.bot
package models

sealed trait Action

object Action:
  case object ShowGreeting extends Action
  case object ShowHelp     extends Action
  case object ShowHistory  extends Action
  case object ShowBudget   extends Action
  case object Ignore       extends Action
  case class  ExecIncome(amount: Int, fullCommandText: String)      extends Action
  case class  ExecExpenditure(amount: Int, fullCommandText: String) extends Action
  case class  ClaimWrongInput(text: String)                         extends Action
  case class  CalcListOfOperations(amountsTransactionInfos: List[(Int, String)]) extends Action

  def fromParse(text: String): Action =
    def parseLine(line: String): Either[String, (Int, String)] =
      val maybeAmountDescription = line.split(" ", 2)
      maybeAmountDescription.length match
        case 2 =>
          val Array(maybeAmount, _) = maybeAmountDescription
          maybeAmount.toIntOption match
            case Some(amount) => Right(amount, line)
            case None => Left(s""""Число не найдено:\n"$line"""")
        case 1 => Left("""в строке нет пробела между числом и описанием\n:"$line"""")

    val lines = text.split("\n").tail
    val parsedLines = lines.map(parseLine)

    parsedLines.filter(_.isLeft) match
      case Array() =>
        CalcListOfOperations(
          parsedLines.map {
            case Right(amount, transactionInfo) =>
              (amount, transactionInfo)
          }.toList
        )

      case unparsedLines =>
        val errorText =
          "Не смог распарсить следущие строки:\n" ++
            unparsedLines
              .map { case (Left(line)) => s""""$line"""" }
              .mkString(",\n")

        ClaimWrongInput(errorText)

  def fromTransactionCommand(text: String): Action =
    def toExecIncomeOrExpenditure(
        moneyflowType: String,
        amount: Int,
      ) =
      moneyflowType match
        case "/доход" => ExecIncome(amount, text)
        case "/расход" => ExecExpenditure(amount, text)
        case _ => Ignore

    val moneyflowtypeAmountDescription = text.split(" ", 3)
    moneyflowtypeAmountDescription.length match
      case 3 =>
        moneyflowtypeAmountDescription(1).toIntOption match
          case Some(amount) =>
            toExecIncomeOrExpenditure(
              moneyflowtypeAmountDescription(0),
              amount,
            )
          case None =>
            ClaimWrongInput("первый аргумент этой команды должен быть числом")

      case 2 =>
        moneyflowtypeAmountDescription(1).toIntOption match
          case Some(_) =>
            ClaimWrongInput("вы забыли добавить описание, попробуйте повторить комманду")
          case None =>
            ClaimWrongInput(
              s"""|комманда $text должна сопровождаться количеством денег (число) и описанием через пробел
                  |
                  |пример: $text 1000 плата за работу"""
            )

      case _ =>
        ClaimWrongInput(
          s"""|комманда $text должна сопровождаться количеством денег и описанием через пробел
                            |
                            |пример: $text 1000 плата за работу"""
        )
