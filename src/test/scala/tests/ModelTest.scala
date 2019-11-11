package tests

import java.sql.Date

import model.{ Job, JobRepository, JobStatus }
import org.scalatest.FlatSpec

class ModelTest extends FlatSpec {
  "Jobs repository" should "insert job" in {
    val jobs = runtime.unsafeRun(for {
      _ <- JobRepository.deleteAllJobs()
      _ <- JobRepository.addJob(Job(DUMMY_DETAILS, JobStatus.New, new Date(System.currentTimeMillis())))
      jobs <- JobRepository.getNewJobs
    } yield jobs)

    assert(jobs.length == 1 && jobs.head.jobStatus == JobStatus.New)
  }
}
