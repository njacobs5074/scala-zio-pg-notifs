package tests

import java.sql.Date

import model.{ Job, JobRepository, JobStatus }
import org.scalatest.FlatSpec
import wrappers.pgnotifs.PostgresIO

class NotificationsTest extends FlatSpec {
  "Jobs repository" should "generate notifications for new jobs" in {

    val (jobs, notifs) = runtime.unsafeRun(for {
      _ <- JobRepository.deleteAllJobs()
      _ <- PostgresIO.startChannelNotifications(JobRepository.JOBS_CHANNEL)
      _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
      notifs <- PostgresIO.getChannelNotifications("jobs_status_channel")
      jobs <- JobRepository.getNewJobs
    } yield (jobs, notifs))

    assert(jobs.length == notifs.length && jobs.exists(_.id == notifs.head))
  }

  it should "stop notifications correctly" in {
    val (notifs01, notifs02, newJobs01, newJobs02) = runtime.unsafeRun(for {
      // Clear the jobs table.
      _ <- JobRepository.deleteAllJobs()

      // Listen to channel notifications, add a job, get a notification, and snapshot the jobs table
      _ <- PostgresIO.startChannelNotifications(JobRepository.JOBS_CHANNEL)
      _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
      notifs01 <- PostgresIO.getChannelNotifications(JobRepository.JOBS_CHANNEL)
      newJobs01 <- JobRepository.getNewJobs

      // Stop listening to channel notifications
      _ <- PostgresIO.stopChannelNotifications(JobRepository.JOBS_CHANNEL)

      // Add another job, check for notifications, snapshot the jobs table again.
      _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
      notifs02 <- PostgresIO.getChannelNotifications(JobRepository.JOBS_CHANNEL)
      newJobs02 <- JobRepository.getNewJobs

    } yield (notifs01, notifs02, newJobs01, newJobs02))

    assert(notifs01.length == 1)
    assert(notifs02.isEmpty)
    assert(newJobs01.length == 1)
    assert(newJobs02.length == 2)
  }
}
