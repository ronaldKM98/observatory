package observatory

object Main extends App {
  val stationsFile = "/stations.csv"
  val tempsFile = "/1975.csv"
  println(parsePath(stationsFile))
}