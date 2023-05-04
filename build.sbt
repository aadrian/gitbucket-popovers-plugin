name := "gitbucket-popovers-plugin"
organization := "io.github.gitbucket"
version := "1.0.0"
scalaVersion := "2.13.10"
gitbucketVersion := "4.39.0"

import scala.sys.process._

def BuildCommand(command: Seq[String]): Seq[String] = {
  val os = sys.props("os.name").toLowerCase
  if (os contains "windows") (Seq("cmd", "/c") ++ command) else command
}

def BuildNpmCommand(command: Seq[String]): Seq[String] = {
  BuildCommand(Seq("npx", "--no-install") ++ command)
}

def ExecCommand(command: Seq[String], builder: (Seq[String]) => Seq[String], log: ProcessLogger): Unit = {
  val ret = Process(builder(command)) ! log
  if(ret != 0)
    throw new MessageOnlyException(s"Failed to run `${command(0)}`")
}

// Compile TypeScript sources
Compile / resourceGenerators += Def.task {
  // Output directory
  val outDir: File = (Compile / resourceManaged).value / "assets"

  // Need to delete the output directory first
  IO.delete(outDir)

  // Run webpack
  val command = Seq("webpack", "--output-path", outDir.getPath)
  ExecCommand(command, BuildNpmCommand, streams.value.log)

  // List all files in 'outDir'
  val finder: PathFinder = (outDir ** "*") filter { _.isFile }
  finder.get
}.taskValue
