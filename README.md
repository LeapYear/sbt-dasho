# sbt-dasho
DashO plugin for sbt

The plugin allows jars packaged by sbt to be protected using
[PreEmptive Protection DashO](https://www.preemptive.com/dasho/pro/userguide/en/index.html).

## Usage

Follow the [installation instructions](https://www.preemptive.com/dasho/pro/userguide/en/install_installation.html) for the
DashO version you are using.

Add the plugin to `project/build.sbt` of your project:

<!-- TODO: When we publish the plugin, this should be updated to use addSbtPlugin in plugins.sbt -->
```
lazy val root = project.in(file(".")).dependsOn(dashO)
lazy val dashO = RootProject(uri("ssh://git@github.com/LeapYear/sbt-dasho.git#<commit-hash>"))
```

In `build.sbt`, load the plugin and point it to your DashO installation. Change dashOHome to point to where you installed
DashO.
```
project
  .enablePlugins(DashOPlugin)
  .settings(
    ...,
    dashOHome := file("/opt/PreEmptive_Protection_DashO_9_0/app")
    dashOVersion := "9.0.0"
  )
```
By default, `dashOHome` corresponds to the `DASHO_HOME` environment variable.

Next, build the jar and protect it using DashO

```
sbt protect
```

From the original jar (for example `root-0.1.jar`), this will create multiple files in the target directory:
* `root-0.1-protected.jar`
* `root-0.1-dashOMapping.txt`
* `root-0.1-dashOReport.txt`

# Maintainers

Install pre-commit in the repo to provide health checks and lints
```
pre-commit install
```

Run pre-commit on all files
```
pre-commit run -av
```

Run the tests
```
DASHO_HOME=/path/to/dasho sbt scripted
```

Code formatting
```
sbt scalafmtAll headerCreate headerCheck scalastyle
```

## macOS installation notes

We provide a Brewfile that gives you all the necessary tools to test this repo.

Install
```
brew bundle
pre-commit install
jenv add $(/usr/libexec/java_home -v1.8)
```

The scripted test framework uses the java on your path, so you may need to run the tests with non-default java version.
```
DASHO_HOME=/path/to/dasho JAVA_HOME=$(/usr/libexec/java_home -v1.8) sbt scripted
```
