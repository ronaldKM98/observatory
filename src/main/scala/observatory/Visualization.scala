package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val p = 2.5
    val errorRange = 1000 // meters

    val weights = temperatures.par.map {
      case (loc, _) =>
        val d = distance(loc, location)
        if (d > errorRange) weight(p)(d) else 1.0
    }

    val sumOfWeights = weights.sum

    val sumOfWeightedTemps = temperatures.par.zip(weights).map {
      case ((loc, temp), weight) =>
        val d = distance(loc, location)
        if (d > errorRange) weight * temp else temp
    }.sum

    sumOfWeightedTemps / sumOfWeights
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    ???
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    ???
  }

  /**
    * Helper functions
    */

  def weight(p: Double)(distance: Double): Double = 1 / math.pow(distance, p)

  def distance(x: Location, xi: Location): Double = {
    val delta_lon = math.toRadians(math.abs(x.lon - xi.lon))
    val x_lat = math.toRadians(x.lat)
    val xi_lat = math.toRadians(xi.lat)
    val radius = 6371000.0
    def antipodes(location: Location, location1: Location): Boolean =
      math.toRadians(location.lat) == math.toRadians(location1.lat * -1) &&
        (math.toRadians(location1.lon) == (if(math.toRadians(location.lon) > 0) math.toRadians(location.lon - 180)
                                            else math.toRadians(location.lon + 180)))

    val omega = {
      if (x_lat == xi_lat) 0
      else if (antipodes(x, xi)) math.Pi
      else math.acos(
        math.sin(x_lat) * math.sin(xi_lat) + math.cos(x_lat) * math.cos(xi_lat) * math.cos(delta_lon)
      )
    }

    radius * omega
  }
}