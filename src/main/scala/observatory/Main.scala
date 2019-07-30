package observatory

import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

import com.sksamuel.scrimage.Image

object Main extends App {

  val stationsFile = "/stations.csv"

  val initialYear: Int = 2015
  val lastYear: Int = 2015

  val years = (initialYear to lastYear).map(_.toString).map("/" + _ + ".csv")

  val colorScale: Iterable[(Temperature, Color)] = Iterable(
    (60,  Color(255, 255, 255)),
    (32,  Color(255, 0, 0)),
    (12,  Color(255, 255, 0)),
    (0,   Color(0, 255, 255)),
    (-15, Color(0, 0, 255)),
    (-27, Color(255, 0, 255)),
    (-50, Color(33, 0, 107)),
    (-60, Color(0, 0, 0))
  )

  val dir = new File("src/main/resources").listFiles()
    .map(file => file.toString)
    .map(file => file.split("/"))
    .map(file => file.last)
    .map("/" + _)

  val intersection = dir.intersect(years)

  for {
    file <- intersection
    if ! file.endsWith(stationsFile)
    zoom <- 0 to 3
    (x, y) <- List((0, 0), (0, 1), (1, 0), (1, 1))
  } yield processTile(file, stationsFile, colorScale, Tile(x, y, zoom))

  Extraction.stop()

  def processTile(temperaturesFile: String,
                  stationsFile: String,
                  colorScale: Iterable[(Temperature, Color)],
                  tile: Tile): Unit = {

    val year: Int = temperaturesFile.substring(1, temperaturesFile.length - 4).toInt

    val temperatures: Iterable[(LocalDate, Location, Temperature)] =
      Extraction.locateTemperatures(year, stationsFile, temperaturesFile)

    val avgTemps: Iterable[(Location, Temperature)] =
      Extraction.locationYearlyAverageRecords(temperatures)

    def image: Image = Interaction.tile(avgTemps, colorScale, tile)

    Interaction.writeImage(year, tile, image)
  }
}