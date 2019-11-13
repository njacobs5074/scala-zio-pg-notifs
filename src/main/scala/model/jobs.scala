package model

import java.sql.ResultSet
import java.sql.Date

import play.api.libs.json.{ JsValue, Json }
import wrappers.pgnotifs.PostgresIO
import zio.ZIO

import scala.language.implicitConversions

object JobStatus extends Enumeration {
  protected case class Val(value: String) extends super.Val
  implicit def valueToJobStatusVal(v: Value): Val = v.asInstanceOf[Val]

  val New = Val("new")
  val Initializing = Val("initializing")
  val Initialized = Val("initialized")
  val Running = Val("running")
  val Success = Val("success")
  val Error = Val("error")

  val valueMap: Map[String, JobStatus.Value] = values.groupBy(_.value).map { case (k, v) =>
      k -> v.head
  }
}

case class Job(details: JsValue,
               jobStatus: JobStatus.Value,
               statusChangeTime: Date,
               id: Long = -1L)

object Job {
  def apply(rs: ResultSet): Job = new Job(
    Json.parse(rs.getString("details")),
    JobStatus.valueMap(rs.getString("status")),
    rs.getDate("status_change_time"),
    rs.getLong("id")
  )
}

object JobRepository {

  // This is also hard-coded in the /resources/db/jobs.sql script
  val JOBS_CHANNEL = "jobs_status_channel"

  def addJob(job: Job): ZIO[PostgresIO, Throwable, Unit] = PostgresIO.effect { connection =>
    val sql = "INSERT INTO jobs(details, status, status_change_time) VALUES(to_json(?::json), ?::job_status, ?)"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, job.details.toString())
    stmt.setString(2, job.jobStatus.value)
    stmt.setDate(3, job.statusChangeTime)
    stmt.executeUpdate()
    stmt.close()
  }

  def getJob(id: Long): ZIO[PostgresIO, Throwable, Option[Job]] = PostgresIO.effect { connection =>
    val sql = "SELECT details, status, status_change_time FROM jobs WHERE id = ?"
    val stmt = connection.prepareStatement(sql)
    stmt.setLong(1, id)
    val results = entries(stmt.executeQuery())(Job.apply).toList
    stmt.close()

    results.headOption
  }

  def getNewJobs: ZIO[PostgresIO, Throwable, List[Job]] = PostgresIO.effect { connection =>
    val sql = "SELECT details, status, status_change_time, id FROM jobs WHERE status = 'new'"
    val stmt = connection.prepareStatement(sql)
    val results = entries(stmt.executeQuery())(Job.apply).toList
    stmt.close()
    results
  }

  def updateJob(job: Job): ZIO[PostgresIO, Throwable, Int] = PostgresIO.effect { connection =>
    val sql = "UPDATE jobs set details = to_json(?::json), status = ?, status_change_time = ? where id = ?"
    val stmt = connection.prepareStatement(sql)
    stmt.setString(1, job.details.toString)
    stmt.setString(2, job.jobStatus.value)
    stmt.setObject(3, job.statusChangeTime)
    stmt.setLong(4, job.id)
    val numUpdated = stmt.executeUpdate()
    stmt.close()
    numUpdated match {
      case 1 =>
        1
      case 0 =>
        throw new RuntimeException(s"Failed to find job with id ${job.id}")
      case other =>
        // Should never happen
        throw new RuntimeException(s"Updated $other jobs with id ${job.id}: check table def'n")
    }
  }

  def claimJob(): ZIO[PostgresIO, Throwable, Option[Job]] = PostgresIO.effect { connection =>
    val sql = """
                |UPDATE jobs SET status=?::job_status
                |WHERE id = (
                |  SELECT id
                |  FROM jobs
                |  WHERE status=?::job_status
                |  ORDER BY id
                |  FOR UPDATE SKIP LOCKED
                |  LIMIT 1
                |)
                |RETURNING *;
                |""".stripMargin

    val stmt = connection.prepareStatement(sql)
    stmt.setObject(1, JobStatus.Initializing.value)
    stmt.setObject(2, JobStatus.New.value)
    val maybeJob = if (stmt.execute()) {
      Option(stmt.getResultSet).flatMap(entries(_)(Job.apply).toList.headOption)
    } else {
      None
    }
    stmt.close()
    maybeJob
  }

  def deleteAllJobs(): ZIO[PostgresIO, Throwable, Int] = PostgresIO.effect { connection =>
    val stmt = connection.createStatement()
    val numDeleted = stmt.executeUpdate("DELETE FROM jobs")
    stmt.close()
    numDeleted
  }
}