import java.nio.file.Paths

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

package object observatory {
  type Temperature = Double // °C, introduced in Week 1
  type Year = Int // Calendar year, introduced in Week 1
  type Month = Int
  type Day = Int
  type ID = (Int, Int)

  def parsePath(resource: String): String =
    Paths.get(getClass.getResource(resource).toURI).toString


  // Spark config
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

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)


}