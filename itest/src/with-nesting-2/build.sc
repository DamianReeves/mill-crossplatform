import $exec.plugins

import mill._
import mill.api.Result
import mill.define.SelectMode
import mill.eval._
import mill.main.{MainModule, RunScript}
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import mill.util.Watched
import ujson.Value
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.10", "3.2.1")

trait CommonScalaModule extends ScalaModule {
  def scalacOptions = super.scalacOptions() ++ Seq("-deprecation", "-feature", "-unchecked", "-Xfatal-warnings")
}
trait CommonJVM extends CommonScalaModule
trait CommonJS extends ScalaJSModule with CommonScalaModule{
  def scalaJSVersion = "1.12.0"
}
trait CommonNative extends ScalaNativeModule with CommonScalaModule{
  def scalaNativeVersion = "0.4.7"
}

trait CommonTests extends TestModule.Munit


object nesting extends Cross[NestingModule](scalaVersions: _*)
class NestingModule(val crossScalaVersion:String) extends CrossPlatform {
  trait NestingModule
  object core extends CrossPlatform {
    trait Shared extends CrossPlatformCrossScalaModule

    object jvm extends Shared with CommonJVM {
      object test extends Tests with CommonTests
    }

    object js extends Shared with CommonJS {
      object test extends Tests with CommonTests
    }

    object native extends Shared with CommonNative {
      object test extends Tests with CommonTests
    }
  }
}
def execute(ev: Evaluator, command: String) = {
  MainModule.evaluateTasksNamed(
    ev,
    Seq(command),
    SelectMode.Single
  )(res => res.collect { case (_,Some(r)) => r
  })
}

def verify(ev:Evaluator) = T.command {
  locally {
    scalaVersions.foreach { scalaVersion =>
      val Result.Success(Watched(Some(Vector((taskName,options), rest @ _*)), _)) = execute(ev, s"""nesting._.core.jvm.test.scalacOptions""")
      println(s"scalacOptions for $taskName: $options")
      assert(
        options == ujson.Arr("-deprecation", "-feature", "-unchecked", "-Xfatal-warnings"),
        "Scalac options are not correct")    }

  }
  locally {
    scalaVersions.foreach { scalaVersion =>
      val Result.Success(Watched(Some(Vector((taskName, options), rest@_*)), _)) = execute(ev, s"""nesting._.core.js.test.scalacOptions""")
      println(s"scalacOptions for $taskName: $options")
      assert(
        options == ujson.Arr("-deprecation", "-feature", "-unchecked", "-Xfatal-warnings"),
        "Scalac options are not correct")
    }

  }
  ()
}