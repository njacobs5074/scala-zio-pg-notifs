import java.sql.ResultSet

package object model {
  /** Convert JDBC ResultSet to a Scala iterator */
  def entries[T](resultSet: ResultSet)(mapper: (ResultSet) => T): Iterator[T] = new Iterator[T] {
      def hasNext: Boolean = resultSet.next()
      def next(): T = mapper(resultSet)
    }
}
