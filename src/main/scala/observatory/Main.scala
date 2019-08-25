package observatory

import java.io.File
import java.time.LocalDate

import com.sksamuel.scrimage.Image
import org.apache.spark.rdd.RDD
import observatory.Year

import scala.collection.parallel.ParSeq

object Main extends App {

  // begin main
  val stationsFile = "/stations.csv"
  val initialYear = 1975
  val lastYear = 1976
  val years: Array[String] = (initialYear to lastYear).map(_.toString).map("/" + _ + ".csv").toArray

  val dir: Array[String] = new File("src/main/resources").listFiles()
    .map(file => file.toString)
    .map(file => file.split("/"))
    .map(file => file.last)
    .map("/" + _)

  val intersection: Array[String] = dir.intersect(years)
  val yearlyData: Iterable[(Year, RDD[(Location, Temperature)])] = calcAverages(stationsFile, intersection.toList)

  this.generateDeviations(yearlyData)
  //this.generateTemperatures(yearlyData)

  spark.stop()

  // end main

  // Functions
  def generateTemperatures(yearlyData: Iterable[(Year, RDD[(Location, Temperature)])]): Unit = {
    Interaction.generateTiles(yearlyData, generateRDDImage)
  }

  def generateDeviations(yearlyData: Iterable[(Year, RDD[(Location, Temperature)])]): Unit = {

    val (from1975to1989: Iterable[(Year, RDD[(Location, Temperature)])],
    from1990to2015: Iterable[(Year, RDD[(Location, Temperature)])]) = yearlyData.partition(_._1 <= 1975)

    val temps: Iterable[Iterable[(Location, Temperature)]] = from1975to1989.map(_._2).map(_.collect().toVector)
    val normals: GridLocation => Temperature = Manipulation.average(temps)

    println("Averages")


    val deviations: ParSeq[(Year, GridLocation => Temperature)] = from1990to2015.toStream.par.map { case(year, rdd) =>
      val devs = Manipulation.deviation(rdd.collect().toVector, normals)
      (year, devs)
    }

    deviations.foreach { grid =>
      val colorScale: Iterable[(Temperature, Color)] = Iterable(
        (60,  Color(255, 255, 255)),
        (32,  Color(255, 0, 0)),
        (12,  Color(255, 255, 0)),
        (0,   Color(0, 255, 255)),
        (-15, Color(0, 0, 255)),
        (-27, Color(255, 0, 255)),
        (-50, Color(33, 0, 107)),
        (-60, Color(0, 0, 0)))

      Visualization2.generateTiles(grid, colorScale)
    }
  }

  def generateRDDImage(year: Year, tile: Tile, data: RDD[(Location, Temperature)]): Unit = {
    val colorScale: Iterable[(Temperature, Color)] = Iterable(
      (60,  Color(255, 255, 255)),
      (32,  Color(255, 0, 0)),
      (12,  Color(255, 255, 0)),
      (0,   Color(0, 255, 255)),
      (-15, Color(0, 0, 255)),
      (-27, Color(255, 0, 255)),
      (-50, Color(33, 0, 107)),
      (-60, Color(0, 0, 0)))

    val image: Image = Interaction.tile(data, colorScale, tile)
    Interaction.writeImage(year, tile, image)
  }

  def calcAverages(stationsFile: String, temperaturesFiles: List[String]):
                                                                Iterable[(Year, RDD[(Location, Temperature)])] = {
    temperaturesFiles.map { temperaturesFile =>
      val year: Int = temperaturesFile.substring(1, temperaturesFile.length - 4).toInt

      val temperatures: RDD[(LocalDate, Location, Temperature)] =
        Extraction.sparkLocateTemperatures(year, stationsFile, temperaturesFile)

      val avgTemps: RDD[(Location, Temperature)] =
        Extraction.sparkAverageRecords(temperatures)

      (year, avgTemps)
    }
  }
}