package observatory

import java.time.LocalDate

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.ScalaReflection.Schema
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

/**
  * 1st milestone: data extraction
  */
object Extraction {
  @transient lazy val conf: SparkConf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("WeatherMonitor")

  @transient val spark: SparkSession =
    SparkSession
      .builder
      .master("local[*]")
      .appName("WeatherMonitor")
      .getOrCreate()

  @transient val sc: SparkContext = spark.sparkContext
  sc.setLogLevel("WARN")

  // For implicit conversions, enables jumping over DF, DS and RDD APIs seamlessly.
  import spark.implicits._

  // Temporary records for spark typing
  case class RawStationRecord(stn: Int, wban: Int, lat: Double, lon: Double)
  case class RawTemperatureRecord(stn: Int, wban: Int, month: Int, day: Int, temp: Double)

  case class FormatStationRecord(id: ID, lat: Double, lon: Double)
  case class FormatTemperatureRecord(id: ID, month: Int, day: Int, temp: Double)


  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String):
                                                                        Iterable[(LocalDate, Location, Temperature)] = {
    val stationSchema: StructType = Encoders.product[RawStationRecord].schema
    val temperatureSchema: StructType = Encoders.product[RawTemperatureRecord].schema

    val stations: Dataset[FormatStationRecord] =
      Extraction.readFile(stationsFile, stationSchema)
        .select($"id", $"lat", $"lon")
        .as[FormatStationRecord]

    val temperatures: Dataset[FormatTemperatureRecord] =
      Extraction.readFile(temperaturesFile, temperatureSchema)
        .select($"id", $"month", $"day", $"temp")
        .as[FormatTemperatureRecord]

    val joint: Dataset[(FormatStationRecord, FormatTemperatureRecord)] =
      stations.joinWith(temperatures, stations("id") === temperatures("id"))

    joint.collect().map(x => helper(year)(x._1, x._2))
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]):
                                                                                  Iterable[(Location, Temperature)] = {
    val ds: Dataset[(Location, Temperature)] =
      spark.createDataset(records.map(record => (record._2, record._3)).toSeq)

    val avg: DataFrame =
      ds.repartition(20).groupBy("_1").avg("_2")

    avg.as[(Location, Temperature)].collect()
  }

  def readFile(path: String, schema: StructType): DataFrame = {
    spark
      .read
      .option(key = "header", value = "false")
      .option(key = "encoding", value = "UTF-8")
      .option(key = "sep", value = ",")
      .schema(schema)
      .csv(parsePath(path))
      .na.fill(0, Seq("stn", "wban"))
      .withColumn("id", TupleUDFs.toTuple2[Int, Int].apply($"stn", $"wban"))
      .map(_.toString())
      .na.drop()
  }

  object TupleUDFs {
    import org.apache.spark.sql.functions.udf
    // type tag is required, as we have a generic udf
    import scala.reflect.runtime.universe.{TypeTag, typeTag}

    def toTuple2[S: TypeTag, T: TypeTag]: UserDefinedFunction =
      udf[(S, T), S, T]((x: S, y: T) => (x, y))
  }

  def helper(year: Int)(location: FormatStationRecord, temperature: FormatTemperatureRecord):
                                                              (LocalDate, Location, Temperature) = {
    (LocalDate.of(year, temperature.month, temperature.day),
      Location(location.lat, location.lon),
      fahrenheitToCelsius(temperature.temp))
  }

  def fahrenheitToCelsius(f: Temperature): Temperature = double2Double((f - 32) / 1.80000)
}