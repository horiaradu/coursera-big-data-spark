package observatory

import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.sql.types._
import util.SparkJob

/**
  * 1st milestone: data extraction
  */
object Extraction extends SparkJob {

  import spark.implicits._
  import org.apache.spark.sql.functions._

  def stations(stationsFile: String) =
    spark.read
      .csv(fsPath(stationsFile))
      .select(
        concat_ws("~", coalesce('_c0, lit("")), '_c1).alias("id"),
        '_c2.alias("latitude").cast(DoubleType),
        '_c3.alias("longitude").cast(DoubleType)
      )
      .where('_c2.isNotNull && '_c3.isNotNull && '_c2 =!= 0.0 && '_c3 =!= 0.0)
      .as[Station]

  def temperatures(year: Int, temperaturesFile: String) =
    spark.read
      .csv(fsPath(temperaturesFile))
      .select(
        concat_ws("~", coalesce('_c0, lit("")), '_c1).alias("id"),
        '_c2.alias("day").cast(IntegerType),
        '_c3.alias("month").cast(IntegerType),
        lit(year).as("year"),
        (('_c4 - 32) * 5 / 9).alias("temperature").cast(DoubleType)
      )
      .where('_c4.between(-200, 200))
      .as[TemperatureRecord]

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Double)] = {
    //  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String) = {
    val stationsDS = stations(stationsFile)
    val temperaturesDS = temperatures(year, temperaturesFile)
    stationsDS.join(temperaturesDS, usingColumn = "id")
      .map(row => (
        LocalDate.of(row(3).asInstanceOf[Int], row(4).asInstanceOf[Int], row(5).asInstanceOf[Int]),
        Location(row(1).asInstanceOf[Double], row(2).asInstanceOf[Double]),
        row(6).asInstanceOf[Double]
      ))
      .collect
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
    ???
  }

  def fsPath(resource: String): String =
    Paths.get(getClass.getResource(resource).toURI).toString

}
