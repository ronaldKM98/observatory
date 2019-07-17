package observatory

object Main extends App {
  val stationsFile = "/stations.csv"
  val tempsFile = "/temperatures.csv"

  val temps = Extraction.locateTemperatures(2015, stationsFile, tempsFile)
  val avgs = Extraction.locationYearlyAverageRecords(temps)

  val values: Iterable[(Temperature, Color)] = Iterable(
    (32, Color(255, 0, 0)),
    (60, Color(255, 255, 255)),
    (-15, Color(0, 0, 255)),
    (0,   Color(0, 255, 255)),
    (-50, Color(33, 0, 107)),
    (-27, Color(255, 0, 255)),
    (12, Color(255, 255, 0)),
    (-60, Color(0, 0, 0))
  )

  Visualization.visualize(avgs, values)
}