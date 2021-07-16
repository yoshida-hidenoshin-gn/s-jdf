package jp.jdf

import com.sun.jna._
import com.typesafe.config.ConfigFactory

import scala.io.Source
import java.io._
import java.nio.file._

class JdfConfig {
    private lazy val config = ConfigFactory.load()
    private lazy val jdfLibBaseName = config.getString("jdf.dlibBaseName")
    private lazy val jdfLibName = System.getProperty("os.name").toLowerCase() match {
      case s if s.contains("mac") => s"""${jdfLibBaseName}-apple.dylib"""
      case s if s.contains("linux") => s"""${jdfLibBaseName}-linux.so"""
      case _ => "-"
    }
    lazy val jdfLibPath = initDylib(jdfLibName)
    private def initDylib(dylibName: String): Path = {
      Native.extractFromResourcePath(s"""/${dylibName}""")
        .toPath
    }

    def jqlFileResource(name: String): Iterator[String] = {
      val pathStr = config.getString(s"""jdf.jqls.${name}""")

      val stream: InputStream = getClass.getResourceAsStream(s"""/${pathStr}""")
      Source.fromInputStream(stream).getLines
    }
}

object JdfConfig extends JdfConfig


trait UseConfig extends JdfConfig {
    val jdfConfig: JdfConfig
}

trait MixinConfig extends UseConfig {
    override val jdfConfig: JdfConfig = JdfConfig
}

