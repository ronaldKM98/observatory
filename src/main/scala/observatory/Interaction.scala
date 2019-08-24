package observatory

import com.sksamuel.scrimage.{Image, Pixel, ScaleMethod}
import org.apache.spark.rdd.RDD


/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  // For implicit conversions, enables jumping over DF, DS and RDD APIs seamlessly.
  import spark.implicits._

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile,
    *         as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    tile.toLocation
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    *         TODO : Implement using spark + akka
    */
  def tile(temperatures: Iterable[(Location, Temperature)],
           colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    Interaction.tile(sc.parallelize(temperatures.toSeq), colors, tile)
  }

  def tile(temperatures: RDD[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    // Most used RDD
    val temps: Iterable[(Location, Temperature)] = temperatures.collect()

    val scale = 2
    val width, height = 256 / scale
    val alpha = 127

    val x_0 = tile.x * width
    val y_0 = tile.y * height

    val stream = (for {
      k <- y_0 until y_0 + height
      j <- x_0 until x_0 + width
    } yield (j, k)).toStream.par

    val pixels: Array[Pixel] = (for {
      (j, k) <- stream
      loc = Tile(j, k, tile.zoom + 8).toLocation
      temp = Visualization.predictTemperature(temps, loc)
      color = Visualization.interpolateColor(colors, temp)
    } yield Pixel(color.red, color.green, color.blue, alpha)).toArray

    Image(width, height, pixels, 1).scaleTo(256, 256, ScaleMethod.Bilinear)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](yearlyData: Iterable[(Year, Data)], generateImage: (Year, Tile, Data) => Unit): Unit = {
    val zoomLevels = 0 to 3
    for {
      (year, data) <- yearlyData.toStream.par
      zoom <- zoomLevels.toStream.par
      tiles = for {
        y <- 0 until Math.pow(2, zoom).toInt
        x <- 0 until Math.pow(2, zoom).toInt
      } yield (x, y)
      (x, y) <- tiles.toStream.par
    } yield generateImage(year, Tile(x, y, zoom), data)
  }

  /**
    * Helper function to save the generated image to the file system with the required format
    * @param year year of the temperatures in the image
    * @param tile corresponding tile of type Tile of the image
    * @param image This is the output of the tile method above
    */
  def writeImage(year: Year, tile: Tile, image: Image): Unit = {
    val file: java.io.File =
      new java.io.File(s"target/temperatures/$year/${tile.zoom}/${tile.x}-${tile.y}.png")

    if (! file.getParentFile.exists) file.getParentFile.mkdirs

    image output file
  }

  def checkFile(year: Year, tile: Tile): Boolean = {
    val file: java.io.File =
      new java.io.File(s"target/temperatures/$year/${tile.zoom}/${tile.x}-${tile.y}.png")

    ! file.exists
  }
}