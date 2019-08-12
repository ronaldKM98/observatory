package observatory

import org.apache.spark.rdd.RDD

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    makeGridRDD(sc.parallelize(temperatures.toSeq))
  }

  def makeGridRDD(temperatures: RDD[(Location, Temperature)]): GridLocation => Temperature = {
    grid: GridLocation => Visualization.predictTemperature(temperatures, Location(grid.lat, grid.lon))
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    averageRDD(temperaturess.map(i => sc.parallelize(i.toSeq)))
  }

  def averageRDD(temperaturess: Iterable[RDD[(Location, Temperature)]]): GridLocation => Temperature = {

    def f (temperaturess: Iterable[RDD[(Location, Temperature)]])(grid: GridLocation): Temperature = {
      temperaturess.map {
        _.filter {
          x => x._1 == Location(grid.lat, grid.lon)
        }
      }.map(_.values)
        .map(temps => temps.sum() / temps.count())
        .sum
    }

    f(temperaturess)
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature):
                                                                                        GridLocation => Temperature = {
    deviationRDD(sc.parallelize(temperatures.toSeq), normals)
  }


  def deviationRDD(temperatures: RDD[(Location, Temperature)], normals: GridLocation => Temperature):
                                                                                        GridLocation => Temperature = {
    def deviations: RDD[(Location, Temperature)] =
      temperatures.map {
        case(loc, temp) =>
          (loc, diff(temp, normals(GridLocation(loc.lat.toInt, loc.lon.toInt))))
      }

    makeGridRDD(deviations)
  }

  def diff(current: observatory.Temperature, normal: observatory.Temperature): Temperature = {
    math.abs(current - normal)
  }
}