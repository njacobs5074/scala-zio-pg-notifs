import java.sql.{ Connection, DriverManager }

import play.api.libs.json.Json
import wrappers.pgnotifs.PostgresIO
import zio.Runtime
import zio.internal.PlatformLive

package object tests {
  val DUMMY_DETAILS = Json.parse(
    """
      |{
      | "data": 20
      |}
      |""".stripMargin
  )

  def runtime: Runtime[PostgresIO] = Runtime(new PostgresIO {
    Class.forName("org.postgresql.Driver")
    override val connection: Connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/zio_pg_notifs")
  }, PlatformLive.Default)
}
