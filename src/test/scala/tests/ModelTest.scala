package tests

import java.sql.{ Connection, Date, DriverManager }

import model.{ Job, JobRepository, JobStatus }
import org.scalatest.FlatSpec
import play.api.libs.json.Json
import wrappers.pgnotifs.PostgresIO
import zio.Runtime
import zio.internal.PlatformLive

class ModelTest extends FlatSpec {
  val DUMMY_DETAILS = Json.parse(
    """
      |{
      | "data": 20
      |}
      |""".stripMargin
  )

  "Jobs repository" should "insert job" in {
    val example =
      for {
        _ <- JobRepository.deleteAllJobs()
        _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
        jobs <- JobRepository.getNewJobs
      } yield jobs

    val runtime: Runtime[PostgresIO] = Runtime(new PostgresIO {
        Class.forName("org.postgresql.Driver")
        override val connection: Connection = DriverManager.getConnection("jdbc:postgresql://localhost:5411/nick", "nick", "")
      }, PlatformLive.Default)

    val jobs = runtime.unsafeRun(example)

    assert(jobs.length == 1)
  }

}
