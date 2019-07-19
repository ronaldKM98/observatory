package observatory

import java.time.LocalDate

import org.scalactic.TolerantNumerics
import org.scalatest.{BeforeAndAfterAll, FunSuite}

trait ExtractionTest extends FunSuite {

  val stationsFile = "/stations.csv"
  val tempsFile = "/temperatures.csv"

  val sampleResult: Iterable[(LocalDate, Location, Temperature)] = Iterable(
    (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
    (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
    (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0)
  )

  val sampleAverages: Iterable[(Location, Temperature)] = Iterable(
    (Location(37.35, -78.433), 27.3),
    (Location(37.358, -78.438), 1.0)
  )

  def colorDistance(a: Color, b: Color): Int = math.abs(a.red - b.red) + math.abs(a.green - b.green) + math.abs(a.blue - b.blue)

  test("locateTemperatures") {
    assert(Extraction.locateTemperatures(2015, stationsFile, tempsFile).size === sampleResult.size)
  }

  test("locationYearlyAverageRecords") {
    assert(Extraction.locationYearlyAverageRecords(sampleResult) === sampleAverages)
  }
}