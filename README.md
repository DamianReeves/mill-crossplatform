# CrossPlatform Mill Plugin

Mill Plugin to simplify Cross platform Mill projects

## Getting Started

Here you can see a basic example using mill-crossplatform

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.0.3`
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule {
  def scalaVersion = "2.13.8"
}

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.7"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.11.0"
}

object core extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule with Common {
    // common `core` settings here
  }
  object jvm extends Shared {
    // jvm specific settings here
  }
  object js extends Shared with CommonJS {
    // js specific settings here
  }
  object native extends Shared with CommonNative {
    // native specific settings here
  }
}

object other extends CrossPlatform {
  // root moduleDeps are correctly applied
  // to all platform submodules
  def moduleDeps = Seq(core)
  trait Shared extends CrossPlatformScalaModule with Common {
    // common `other` settings here
  }
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}
```

## Known issues

### Issues with `PublishModule`s

Mill had [a bug](https://github.com/com-lihaoyi/mill/issues/2082) handling `moduleDeps` in super traits when `PublishModules`.
If you use `PublishModule` you need to update Mill to a version higher on equal than
`0.10.8-9-dd81ad`

### `Cross` module with different sets of Scala versions

Having different Scala versions for the various platform is not supported.
This plugin assumes that all platforms share the same `scalaVersion`.

## Advanced use cases

## Platform specific code

You can place platform specific code in:

```scala
// jvm specific code
millSourcePath / "jvm" / "src"

// directory used in js and jvm modules but not in native
// the directory names are sorted alphabetically.
// it's `js-jvm`, not `jvm-js`.
millSourcePath / "js-jvm" / "src"

// code shared between js and native
// used when the scalaVersion is a Scala 3
// version
millSourcePath / "js-native" / "src-3"
```


### Supporting multiple Scala versions

It is possible to use `CrossPlatform` together with `Cross`
to cross-compile for multiple Scala versions:

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.0.3`
import com.github.lolgab.mill.crossplatform._

trait CommonNative extends ScalaNativeModule {
  def scalaNativeVersion = "0.4.7"
}
trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.11.0"
}

val scalaVersions = Seq("2.13.10", "3.2.0")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  // Note `CrossPlatformCrossScalaModule` instead of `CrossPlatformScalaModule`
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  object js extends Shared with CommonJS
  object native extends Shared with CommonNative
}
```

### Supporting multiple Scala.js / Native versions

It is possible to use `CrossPlatform` together with `Cross`
in the inner modules to cross-compile for multiple Scala.js / Scala Native versions.
Root `moduleDeps` and `compileModuleDeps` work as expected

```scala
import mill._, mill.scalalib._, mill.scalajslib._, mill.scalanativelib._
import $ivy.`com.github.lolgab::mill-crossplatform::0.0.3`
import com.github.lolgab.mill.crossplatform._

val scalaVersions = Seq("2.13.0", "3.2.0")
val scalaJSVersions = Seq("1.11.0")

object core extends Cross[CoreModule](scalaVersions: _*)
class CoreModule(val crossScalaVersion: String) extends CrossPlatform {
  trait Shared extends CrossPlatformCrossScalaModule
  object jvm extends Shared
  // the cross-module should have only one parameter named `val crossScalaJSVersion: String`
  // for it to work correctly. Extend `CrossScalaJSModule` to which requires it.
  class JSModule(val crossScalaJSVersion: String) extends Shared with CrossScalaJSModule
  object js extends Cross[JSModule](scalaJSVersions: _*)
}
```
