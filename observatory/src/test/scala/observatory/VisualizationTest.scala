package observatory


import java.time.LocalDate

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Checkers {
  lazy val locateTemperatures: Iterable[(LocalDate, Location, Double)] = Extraction.locateTemperatures(year, stationsPath, temperaturePath)
  lazy val locateAverage: Iterable[(Location, Double)] = Extraction.locationYearlyAverageRecords(locateTemperatures)
  val year = 1973
  val debug = true
  val stationsPath: String = "/stations.csv"
  val temperaturePath: String = s"/$year.csv"

  test("locationYearlyAverageRecords") {
    locateAverage.take(40).foreach(println)
    assert(locateAverage.count(_._1 == Location(70.933, -8.667)) === 1)
    assert(locateAverage.size === 35)
  }

  test("interpolateColor") {
    val palette = List(
      (100.0, Color(255, 255, 255)),
      (50.0, Color(0, 0, 0)),
      (0.0, Color(255, 0, 128))
    )

    assert(Visualization.interpolateColor(palette, 50.0) === Color(0, 0, 0))
    assert(Visualization.interpolateColor(palette, 0.0) === Color(255, 0, 128))
    assert(Visualization.interpolateColor(palette, -10.0) === Color(255, 0, 128))
    assert(Visualization.interpolateColor(palette, 200.0) === Color(255, 255, 255))
    assert(Visualization.interpolateColor(palette, 75.0) === Color(128, 128, 128))
    assert(Visualization.interpolateColor(palette, 25.0) === Color(128, 0, 64))
  }

  test("Distance 0.0") {
    assert(Visualization.predictTemperature(locateAverage, Location(70.933, -8.667)).round === -2)
    assert(Visualization.predictTemperature(locateAverage, Location(69.017, 23.067)).round === -1)
  }

  test("distance between two locations") {
    assert(Location(0, 0).distance(Location(0, 0)) === 0.0)
    assert(Location(50, 0).distance(Location(60, 0)).round === 1112)
    assert(Location(50, 30).distance(Location(60, 40)).round === 1279)
    assert(Location(50, 30).distance(Location(-60, -40)).round === 13744) //13740
    assert(Location(-50, 30).distance(Location(-60, -40)).round === 4375) //13740
  }

  test("predictTemperature small sets") {
    assert(Visualization.predictTemperature(List((Location(0.0, 0.0), 10.0)), Location(0.0, 0.0)) === 10.0)
    assert(Visualization.predictTemperature(List((Location(45.0, -90.0), 10.0), (Location(-45.0, 0.0), 20.0)), Location(0.0, -45.0)).round === 15.0)
    assert(Visualization.predictTemperature(List((Location(45.0, -90.0), 0.0), (Location(-45.0, 0.0), 59.028308521858634)), Location(0.0, 0.0)).round === 52)
  }

  test("pixelToLocation") {
    assert(Visualization.pixelToLocation(0) === Location(90.0, -180.0))
    assert(Visualization.pixelToLocation(32580) === Location(0.0, 0.0))
    assert(Visualization.pixelToLocation(64799) === Location(-89.0, 179.0))
  }

  //  test("visualize") {
  //    val palette = List(
  //      (60.0, Color(255, 255, 255)),
  //      (32.0, Color(255, 0, 0)),
  //      (12.0, Color(255, 255, 0)),
  //      (0.0, Color(0, 255, 255)),
  //      (-15.0, Color(0, 0, 255)),
  //      (-27.0, Color(255, 0, 255)),
  //      (-50.0, Color(33, 0, 107)),
  //      (-60.0, Color(0, 0, 0))
  //    )
  //
  //    val img = Visualization.visualize(locateAverage, palette)
  //
  //    img.output(new java.io.File(s"../../src/test/resources/$year.png"))
  //
  //    assert(img.pixels.length === 360 * 180)
  //  }
}
