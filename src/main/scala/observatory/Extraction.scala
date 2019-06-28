package observatory

import java.time.LocalDate

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._

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

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String):
                                                                        Iterable[(LocalDate, Location, Temperature)] = {
    val stations: DataFrame =
      readFile(stationsFile)
      .withColumn("lat", $"_c2")
      .withColumn("lon", $"_c3")

    val temperatures: DataFrame =
        readFile(temperaturesFile)
      .withColumn("month", $"_c2")
      .withColumn("day", $"_c3")
      .withColumn("temp", $"_c4")

    val joint: DataFrame =
      stations.join(temperatures, Seq("id"))

    joint.
      select($"lat", $"lon", $"month", $"day", $"temp")
      .collect()
      .map(row => (
        LocalDate.of(year, row(2).asInstanceOf[Int], row(3).asInstanceOf[Int]),
        Location(row(0).asInstanceOf[Double], row(1).asInstanceOf[Double]),
        fahrenheitToCelsius(row(4).asInstanceOf[Temperature]))
      )
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

  def readFile(path: String): DataFrame = {
    spark
      .read
      .option(key = "header", value = "false")
      .option(key = "encoding", value = "UTF-8")
      .option(key = "sep", value = ",")
      .option(key = "inferSchema", value = "true")
      .csv(parsePath(path))
      .na.fill(0, Array("_c0", "_c1"))
      .withColumn("id", TupleUDFs.toTuple2[Int, Int].apply($"_c0", $"_c1"))
      .na.drop()
  }

  def fahrenheitToCelsius(f: Temperature): Temperature = (f - 32) / 1.80000

  object TupleUDFs {
    import org.apache.spark.sql.functions.udf
    // type tag is required, as we have a generic udf
    import scala.reflect.runtime.universe.{TypeTag, typeTag}

    def toTuple2[S: TypeTag, T: TypeTag]: UserDefinedFunction =
      udf[(S, T), S, T]((x: S, y: T) => (x, y))
  }
}