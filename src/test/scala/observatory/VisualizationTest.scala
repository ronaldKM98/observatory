package observatory


import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalactic.TolerantNumerics

trait VisualizationTest extends FunSuite with Checkers {

  implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(0.01)

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

  test("distance helper function") {
    val medellin = Location(6.185700, -75.584626)
    val las_vegas = Location(36.123204, -115.204533)
    assert(Visualization.distance(medellin, las_vegas) === 5225246.35)
  }

  test("interpolateColor") {
    assert(Visualization.interpolateColor(values, 32) === Color(255, 0, 0))
  }

  test("indexes helper function") {
    assert(
      Visualization.indexes(values.toList.sortBy(_._1).reverse, values.head, values.head, 18)
        === (( (32, Color(255, 0, 0)), (12, Color(255, 255, 0))))
    )
  }
}