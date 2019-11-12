package wrappers.pgnotifs

import java.sql.Connection

import org.postgresql.PGConnection
import zio.ZIO

trait PostgresIO {
  def connection: Connection
}

object PostgresIO {

  def effect[A](k: Connection => A): ZIO[PostgresIO, Throwable, A] =
    ZIO.fromFunctionM(env => ZIO.effect(k(env.connection)))

  def startChannelNotifications(channelName: String): ZIO[PostgresIO, Throwable, Unit] = effect { connection =>
    connection.unwrap(classOf[PGConnection])

    val stmt = connection.createStatement()
    stmt.execute(s"LISTEN $channelName")
    stmt.close()
  }

  def stopChannelNotifications(channelName: String): ZIO[PostgresIO, Throwable, Unit] = effect { connection =>
    connection.unwrap(classOf[PGConnection])

    val stmt = connection.createStatement()
    stmt.execute(s"UNLISTEN $channelName")
    stmt.close()
  }

  /** Get the ID's from any database records that have been created or updated. We assume that record ID's are longs  */
  def getChannelNotifications(channelName: String): ZIO[PostgresIO, Throwable, List[Long]] = effect { connection =>
    Option(connection.unwrap(classOf[PGConnection]).getNotifications)
      .map(_.toList.filter(_.getName == channelName).map(_.getParameter.toLong))
      .getOrElse(List.empty)
  }
}
