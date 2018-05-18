val catsV = "1.1.0"
val catsEffectV = "0.10.1"
val log4sV = "1.6.1"
val specs2V = "4.2.0"

lazy val log4cats = project.in(file("."))
  .aggregate(
    coreJVM,
    coreJS,
    testingJVM,
    testingJS,
    scribeJVM,
    scribeJS,
    log4sJVM,
    log4sJS,
    iolog4s,
    docs
  )
  .settings(noPublishSettings)
  .settings(commonSettings, releaseSettings)

lazy val docs = project.in(file("docs"))
  .settings(noPublishSettings)
  .settings(commonSettings, micrositeSettings)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(TutPlugin)
  .dependsOn(log4sJVM)

lazy val core = crossProject.in(file("core"))
  .settings(commonSettings, releaseSettings)
  .settings(
    name := "log4cats-core"
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val testing = crossProject.in(file("testing"))
  .settings(commonSettings, releaseSettings)
  .dependsOn(core)
  .settings(
    name := "log4cats-testing"
  )

lazy val testingJVM = testing.jvm
lazy val testingJS = testing.js

lazy val log4s = crossProject.in(file("log4s"))
  .settings(commonSettings, releaseSettings)
  .dependsOn(core)
  .settings(
    name := "log4cats-log4s",
    libraryDependencies ++= Seq(
      "org.log4s"                   %%% "log4s"                      % log4sV
    )
  )

lazy val log4sJVM = log4s.jvm
lazy val log4sJS = log4s.js

lazy val scribe = crossProject.in(file("scribe"))
  .settings(commonSettings, releaseSettings)
  .dependsOn(core)
  .settings(
    name := "log4cats-scribe",
    libraryDependencies ++= Seq(
      "com.outr" %%% "scribe" % "2.4.0"
    )
  )

lazy val scribeJVM = scribe.jvm
lazy val scribeJS = scribe.js

lazy val iolog4s = project.in(file("iolog4s"))
  .settings(commonSettings, releaseSettings)
  .dependsOn(coreJVM)
  .settings(
    name := "log4cats-iolog4s",
    libraryDependencies ++= Seq(
      "org.iolog4s" %% "iolog4s" % "0.0.4"
    )
  )

lazy val contributors = Seq(
  "ChristopherDavenport" -> "Christopher Davenport"
)

lazy val commonSettings = Seq(
  organization := "io.chrisdavenport",

  scalaVersion := "2.12.6",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12"),

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.6" cross CrossVersion.binary),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),

  libraryDependencies ++= Seq(
    "org.typelevel"               %%% "cats-core"                  % catsV,
    "org.typelevel"               %%% "cats-effect"                % catsEffectV,

    "org.specs2"                  %%% "specs2-core"                % specs2V       % Test
    // "org.specs2"                  %% "specs2-scalacheck"          % specs2V       % Test
  )
)

lazy val releaseSettings = {
  import ReleaseTransformations._
  Seq(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      // For non cross-build projects, use releaseStepCommand("publishSigned")
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    ),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= (
      for {
        username <- Option(System.getenv().get("SONATYPE_USERNAME"))
        password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
      } yield
        Credentials(
          "Sonatype Nexus Repository Manager",
          "oss.sonatype.org",
          username,
          password
        )
    ).toSeq,
    publishArtifact in Test := false,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ChristopherDavenport/log4cats"),
        "git@github.com:ChristopherDavenport/log4cats.git"
      )
    ),
    homepage := Some(url("https://github.com/ChristopherDavenport/log4cats")),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    publishMavenStyle := true,
    pomIncludeRepository := { _ =>
      false
    },
    pomExtra := {
      <developers>
        {for ((username, name) <- contributors) yield
        <developer>
          <id>{username}</id>
          <name>{name}</name>
          <url>http://github.com/{username}</url>
        </developer>
        }
      </developers>
    }
  )
}

lazy val micrositeSettings = Seq(
  micrositeName := "log4cats",
  micrositeDescription := "Functional Logging",
  micrositeAuthor := "Christopher Davenport",
  micrositeGithubOwner := "ChristopherDavenport",
  micrositeGithubRepo := "log4cats",
  micrositeBaseUrl := "/log4cats",
  micrositeDocumentationUrl := "https://christopherdavenport.github.io/log4cats",
  micrositeFooterText := None,
  micrositeHighlightTheme := "atom-one-light",
  micrositePalette := Map(
    "brand-primary" -> "#3e5b95",
    "brand-secondary" -> "#294066",
    "brand-tertiary" -> "#2d5799",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  fork in tut := true,
  scalacOptions in Tut --= Seq(
    "-Xfatal-warnings",
    "-Ywarn-unused-import",
    "-Ywarn-numeric-widen",
    "-Ywarn-dead-code",
    "-Ywarn-unused:imports",
    "-Xlint:-missing-interpolator,_"
  ),
  libraryDependencies += "com.47deg" %% "github4s" % "0.18.4",
  micrositePushSiteWith := GitHub4s,
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN")
)

// Not Used Currently
lazy val mimaSettings = {
  import sbtrelease.Version
  def mimaVersion(version: String) = {
    Version(version) match {
      case Some(Version(major, Seq(minor, patch), _)) if patch.toInt > 0 =>
        Some(s"${major}.${minor}.${patch.toInt - 1}")
      case _ =>
        None
    }
  }

  Seq(
    mimaFailOnProblem := mimaVersion(version.value).isDefined,
    mimaPreviousArtifacts := (mimaVersion(version.value) map {
      organization.value % s"${moduleName.value}_${scalaBinaryVersion.value}" % _
    }).toSet,
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq()
    }
  )
}

lazy val noPublishSettings = {
  import com.typesafe.sbt.pgp.PgpKeys.publishSigned
  Seq(
    publish := {},
    publishLocal := {},
    publishSigned := {},
    publishArtifact := false
  )
}
