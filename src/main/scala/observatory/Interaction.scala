package observatory

import java.awt.image.BufferedImage

import com.sksamuel.scrimage.{Image, Pixel}

import scala.collection.parallel.immutable.ParSeq

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

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
    */
  def tile(temperatures: Iterable[(Location, Temperature)],
           colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val width = 256
    val height = 256
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
      temp = Visualization.predictTemperature(temperatures, loc)
      color = Visualization.interpolateColor(colors, temp)
    } yield Pixel(color.red, color.green, color.blue, alpha)).toArray

    Image(width, height, pixels, BufferedImage.TYPE_INT_RGB)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](yearlyData: Iterable[(Year, Data)], generateImage: (Year, Tile, Data) => Unit): Unit = {
    ???
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

    if (!file.getParentFile.exists) file.getParentFile.mkdirs

    image output file
  }


}