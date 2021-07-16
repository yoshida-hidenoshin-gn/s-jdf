package jp.jdf

import com.sun.jna._
import com.typesafe.config.ConfigFactory


trait JdfStatement {
    def toString: String
}

case class Statement(
    selector: String,
    alias: String,
    condition: Option[String],
    left: Option[String],
    operator: Option[String],
    right: Option[String],
    addon: Option[String]
) extends JdfStatement {
    override def toString: String = {
      if (condition.isEmpty) {
        Array(selector, "AS", alias).mkString(" ")

      } else {
        if (left.isEmpty || operator.isEmpty || right.isEmpty || addon.isEmpty) {
          Array(selector, "AS", alias, condition.get).mkString(" ")

        } else if (addon.nonEmpty) {
          (for {
            c <- condition
            l <- left
            op <- operator
            r <- right
            a <- addon
          } yield Array(selector, "AS", alias, c, l, op, r, a).mkString(" "))
            .getOrElse("")

        } else {
          (for {
            c <- condition
            l <- left
            op <- operator
            r <- right
          } yield Array(selector, "AS", alias, c, l, op, r).mkString(" "))
            .getOrElse("")
        }
      }
    }
}


trait JdfRaw extends Library {
    def flatten(json_s: String): String
    def query(json_s: String, statements: String): String
}


class Jdf extends MixinConfig {
    private lazy val jdf = Native.load(jdfConfig.jdfLibPath.toString, classOf[JdfRaw])

    private def statementsFromString(s: String): Option[Statement] = {
      val arr = s.split(" ")
      arr.length match {
        case n if (n == 0) => None
        case n if (n == 3) => Some(new Statement(arr(0), arr(2), None, None, None, None, None))
        case n if (n == 4) => Some(new Statement(arr(0), arr(2), Some(arr(3)), None, None, None, None))
        case n if (n == 7) => arr(3) match {
          case "ARRAY_MAP" => Some(new Statement(arr(0), arr(2), Some(arr(3)), None, None, None, Some(arr(6))))
          case "WHEN" => Some(new Statement(arr(0), arr(2), Some(arr(3)), Some(arr(4)), Some(arr(5)), Some(arr(6)), None))
          case _ => None
        }
      }
    }

    private def rawQuery(jsonRawStr: String, statements: String): Option[String] = {
        if (jsonRawStr.isEmpty || statements.isEmpty) {
          None
        } else {
          Some(jdf.query(jsonRawStr, statements))
        }
    }

    def statementsFromJql(name: String): Array[Statement] = {
      jdfConfig
        .jqlFileResource(name)
        .map(itr => statementsFromString(itr).get)
        .toArray
    }

    def flatten(json_s: String): String = {
       jdf.flatten(json_s)
    }

    def query(jsonRawStr: String, statements: Array[Statement]): Option[String] = {
        val stmtsStr = statements
          .map(stmt => stmt.toString)
          .mkString("")

        rawQuery(jsonRawStr, stmtsStr) match {
          case Some(s) => if (s == "" || s == "{}") {
              None
            } else {
              Some(s)
            }
          case None => None
        }
    }
}

object Jdf extends Jdf
