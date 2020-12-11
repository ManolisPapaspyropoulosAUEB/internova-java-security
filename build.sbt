lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """internova""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      guice,
      javaJpa,
      javaJdbc,
      javaCore,
      "mysql" % "mysql-connector-java" % "5.1.38",
      "org.hibernate" % "hibernate-core" % "5.4.9.Final",
      javaWs % "test",
      "org.apache.poi" % "poi" % "3.17",
      "org.apache.poi" % "poi-ooxml" % "3.17",
      "org.awaitility" % "awaitility" % "4.0.1" % "test",
      "org.assertj" % "assertj-core" % "3.14.0" % "test",
      "org.apache.pdfbox" % "pdfbox" % "2.0.1",
      "org.mockito" % "mockito-core" % "3.1.0" % "test"),


    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked"),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror"),
    PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml"
  )
libraryDependencies += guice



//https://stackoverflow.com/questions/61812492/intellij-idea-sbt-refresh-fetcherrordownloadingartifacts-npm-4-2-0-sources-jar
//https://github.com/sbt/sbt/issues/5501