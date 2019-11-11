package tests

import java.sql.Date

import model.{ Job, JobRepository, JobStatus }
import org.scalatest.FlatSpec
import wrappers.pgnotifs.PostgresIO

class NotificationsTest extends FlatSpec {
  "Jobs repository" should "generate notifications for new jobs" in {

    val (jobs, notifs) = runtime.unsafeRun(for {
      _ <- JobRepository.deleteAllJobs()
      _ <- PostgresIO.startChannelNotifications("jobs_status_channel")
      _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
      notifs <- PostgresIO.getChannelNotifications("jobs_status_channel")
      jobs <- JobRepository.getNewJobs
    } yield (jobs, notifs))

    assert(jobs.length == notifs.length && jobs.exists(_.id == notifs.head))
  }
}
