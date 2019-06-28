package observatory

import java.time.LocalDate

import org.scalatest.{FunSuite, BeforeAndAfterAll}

trait ExtractionTest extends FunSuite {

  val stationsFile = "/Users/ronald/Documents/observatory/observatory/src/test/resources/stations.csv"
  val tempsFile = "/Users/ronald/Documents/observatory/observatory/src/test/resources/temperatures.csv"

  val sampleResult: Iterable[(LocalDate, Location, Temperature)] = Iterable(
    (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
    (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
    (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0)
  )

  val sampleAverages: Iterable[(Location, Temperature)] = Iterable(
    (Location(37.35, -78.433), 27.3),
    (Location(37.358, -78.438), 1.0)
  )

  test("locateTemperatures") {
    assert(Extraction.locateTemperatures(2015, stationsFile, tempsFile).size === sampleResult.size)
  }

  test("locationYearlyAverageRecords") {
    assert(Extraction.locationYearlyAverageRecords(sampleResult) === sampleAverages)
  }
}