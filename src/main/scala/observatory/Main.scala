package observatory

object Main extends App {
  val stationsFile = "/stations.csv"
  val tempsFile = "/1975.csv"

  Extraction.locateTemperatures(1975, stationsFile, tempsFile) take 5 foreach println



}