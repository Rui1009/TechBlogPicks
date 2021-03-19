import sbt.file
import slick.codegen.SourceCodeGenerator

import scala.reflect.io.Directory

object CodeGenerator {
  def gen(): Unit = {
    val directory = new Directory(file("./app/infra/dto"))
    println(directory.isDirectory)
    directory.deleteRecursively()

    SourceCodeGenerator.main(
      Array(
        "slick.jdbc.PostgresProfile",
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/tech_blog_picks_server",
        "app",
        "infra.dto",
        sys.env.getOrElse("DB_USER", ""),
        sys.env.getOrElse("DB_PASSWORD", ""),
        "true",
        "slick.codegen.SourceCodeGenerator",
        "true"
      )
    )
  }

}
