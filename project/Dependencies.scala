import sbt._

object Dependencies {
  object V {
    val catsCore    = "2.8.0"
    val catsEffect  = "3.3.14"
    val doobie      = "1.0.0-RC2"
    val pureconfig  = "0.17.1"
    val slf4j       = "1.7.36"
    val telegramium = "7.62.0"
  }

  val `budget-bot`: Seq[ModuleID] = Seq(
    "org.typelevel"         %% "cats-core"        % V.catsCore,
    "org.typelevel"         %% "cats-effect"      % V.catsEffect,
    "org.tpolecat"          %% "doobie-core"      % V.doobie,
    "org.tpolecat"          %% "doobie-hikari"    % V.doobie,
    "org.tpolecat"          %% "doobie-postgres"  % V.doobie,
    "org.tpolecat"          %% "doobie-scalatest" % V.doobie % Test,
    "com.github.pureconfig" %% "pureconfig-core"  % V.pureconfig,
    "org.slf4j"             %  "slf4j-api"        % V.slf4j,
    "org.slf4j"             %  "slf4j-simple"     % V.slf4j,
    "io.github.apimorphism" %% "telegramium-core" % V.telegramium,
    "io.github.apimorphism" %% "telegramium-high" % V.telegramium,
  )
}
