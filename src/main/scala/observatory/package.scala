import java.nio.file.Paths

package object observatory {
  type Temperature = Double // °C, introduced in Week 1
  type Year = Int // Calendar year, introduced in Week 1
  type Month = Int
  type Day = Int
  type ID = (Int, Int)

  def parsePath(resource: String): String =
    Paths.get(getClass.getResource(resource).toURI).toString
}