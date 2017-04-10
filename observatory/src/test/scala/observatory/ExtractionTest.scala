package observatory

import java.time.LocalDate

import org.apache.spark.sql.{DataFrame, Dataset}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import util.SparkJob

@RunWith(classOf[JUnitRunner])
class ExtractionTest extends FunSuite with SparkJob {
  val year = 1975
  val stationsPath: String = "/stations.csv"
  val temperaturePath: String = s"/$year.csv"

  lazy val stations: Dataset[Station] = Extraction.stations(stationsPath).persist
  lazy val temperatures: Dataset[TemperatureRecord] = Extraction.temperatures(year, temperaturePath).persist
  lazy val locateTemperatures: Iterable[(LocalDate, Location, Double)] = Extraction.locateTemperatures(year, stationsPath, temperaturePath)

  test("stations") {
    stations.show()
    assert(stations.filter((station: Station) => station.id == "007005").count() === 0, "id: 007005")
    assert(stations.filter((station: Station) => station.id == "007018").count() === 0, "id: 007018")
    assert(stations.filter((station: Station) => station.id == "725346~94866").count() === 1, "id: 725346~94866")
    assert(stations.filter((station: Station) => station.id == "725346").count() === 1, "id: 725346")
    assert(stations.filter((station: Station) => station.id == "~68601").count() === 1, "id: ~68601")
    assert(stations.count() === 27708, "Num stations")
  }

  test("temperatures") {
    temperatures.show()
    assert(temperatures.filter((tr: TemperatureRecord) => tr.id == "010010").count() === 363, "id: 010010")
    assert(temperatures.filter((tr: TemperatureRecord) => tr.id == "010010" && tr.day == 1 && tr.month == 1 && tr.temperature == (23.2 - 32) * 5 / 9).count() === 1, "id: 010010")
  }

  test("locateTemperatures") {
    locateTemperatures.take(20).foreach(println)
    assert(locateTemperatures.count(_._2 == Location(70.933, -8.667)) === 363)
    assert(locateTemperatures.size === 2176493)
  }

}