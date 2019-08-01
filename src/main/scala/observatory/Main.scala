package observatory

import java.io.File
import java.time.LocalDate

import com.sksamuel.scrimage.Image

object Main extends App {

  // begin main

  val stationsFile = "/stations.csv"
  val initialYear = 2015
  val lastYear = 2015
  val years: Array[String] = (initialYear to lastYear).map(_.toString).map("/" + _ + ".csv").toArray

  val dir: Array[String] = new File("src/main/resources").listFiles()
    .map(file => file.toString)
    .map(file => file.split("/"))
    .map(file => file.last)
    .map("/" + _)

  val intersection: Array[String] = dir.intersect(years)
  val yearlyData: Iterable[(Year, Iterable[(Location, Temperature)])] = calcAverages(stationsFile, intersection.toList)

  Extraction.stop()

  Interaction.generateTiles(yearlyData, generateImage)

  // end main


  // Functions
  def generateImage(year: Year, tile: Tile, data: Iterable[(Location, Temperature)]): Unit = {
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
    val image: Image = Interaction.tile(data, colorScale, tile)
    Interaction.writeImage(year, tile, image)
  }

  def calcAverages(stationsFile: String, temperaturesFiles: List[String]):
                                                                Iterable[(Year, Iterable[(Location, Temperature)])] = {
    temperaturesFiles.map { temperaturesFile =>
      val year: Int = temperaturesFile.substring(1, temperaturesFile.length - 4).toInt

      val temperatures: Iterable[(LocalDate, Location, Temperature)] =
        Extraction.locateTemperatures(year, stationsFile, temperaturesFile)

      val avgTemps: Iterable[(Location, Temperature)] =
        Extraction.locationYearlyAverageRecords(temperatures)

      (year, avgTemps)
    }
  }
}