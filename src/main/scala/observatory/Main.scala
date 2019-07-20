package observatory

object Main extends App {
  val stationsFile = "/stations.csv"
  val tempsFile = "/1975.csv"

  val temps = Extraction.locateTemperatures(2015, stationsFile, tempsFile)
  val avgs = Extraction.locationYearlyAverageRecords(temps)

  val values: Iterable[(Temperature, Color)] = Iterable(
    (60,  Color(255, 255, 255)),
    (32,  Color(255, 0, 0)),
    (12,  Color(255, 255, 0)),
    (0,   Color(0, 255, 255)),
    (-15, Color(0, 0, 255)),
    (-27, Color(255, 0, 255)),
    (-50, Color(33, 0, 107)),
    (-60, Color(0, 0, 0))
  )

  Extraction.stop()

  val image = Visualization.visualize(avgs, values)

  Visualization.outputImage(image, new java.io.File("target/mapa.png"))
}