package observatory

import scala.math._

case class Location(lat: Double, lon: Double) {
  val earthRadius = 6371 // 6371 km

  /**
    * https://en.wikipedia.org/wiki/Great-circle_distance
    *
    * @param other
    * @return
    */
  def distance(other: Location) = other match {
    case Location(otherLat, otherLon) =>
      val x = toRadians(lat)
      val otherX = toRadians(otherLat)

      val deltaX = toRadians(otherLat - lat)
      val deltaY = toRadians(otherLon - lon)

      val a = pow(sin(deltaX / 2), 2) + cos(x) * cos(otherX) * pow(sin(deltaY / 2), 2)
      val greatCircleDist = 2 * atan2(sqrt(a), sqrt(1 - a))

      earthRadius * greatCircleDist
  }
}

case class Color(red: Int, green: Int, blue: Int)

case class Station(id: String, latitude: Double, longitude: Double)

case class TemperatureDate(day: Int, month: Int, year: Int)

case class TemperatureRecord(id: String, day: Int, month: Int, year: Int, temperature: Double)