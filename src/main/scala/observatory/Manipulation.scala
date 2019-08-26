package observatory


import scala.collection.mutable
import scala.collection.parallel.immutable.ParMap

/**
  * 4th milestone: value-added information
  * TODO: Why is this code breaking memory constraints ?
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val keys = for {
      lat <- -89 to 90 by 1
      lon <- -180 to 179 by 1
    } yield GridLocation(lat, lon)

    val map: ParMap[GridLocation, Temperature] =
      keys.toStream.par.map { gridLoc =>
        (gridLoc, Visualization.predictTemperature(temperatures, Location(gridLoc.lat, gridLoc.lon)))
      }.toMap

    grid: GridLocation => map.getOrElse(grid, 0)
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    val groupedByLocation: Map[Location, Iterable[(Location, Temperature)]] = temperaturess.flatten.groupBy(_._1)

    val locationAvgs: Iterable[(Location, Temperature)] =
      groupedByLocation.toStream.par.map{ case(loc, it) =>
        (loc, it.map(_._2).sum / it.size)
      }.toVector

    makeGrid(locationAvgs)
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature):
                                                                                        GridLocation => Temperature = {
    val deviations: Iterable[(Location, Temperature)] =
      temperatures.toStream.par.map {
        case(loc, temp) =>
          (loc, diff(temp, normals(GridLocation(loc.lat.toInt, loc.lon.toInt))))
      }.toVector

    makeGrid(deviations)
  }

  def diff(current: observatory.Temperature, normal: observatory.Temperature): Temperature = {
    current - normal
  }
}