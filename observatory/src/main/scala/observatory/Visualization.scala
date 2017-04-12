package observatory

import com.sksamuel.scrimage.{Image, RGBColor}

import scala.math._

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val pValue = 3 // >= 2
  /**
    * https://en.wikipedia.org/wiki/Inverse_distance_weighting
    *
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {
    val distances = temperatures
      .map { case (loc, temp) => (location.distance(loc), temp) }
    distances.find(_._1 == 0) match {
      case Some((_, temp)) => temp
      case None =>
        val (numerator, denominator) = distances
          .aggregate((0.0, 0.0))(
            {
              case ((numeratorAcc, denominatorAcc), (distance, temp)) => {
                val term = 1 / pow(distance, pValue)
                (term * temp + numeratorAcc, term + denominatorAcc)
              }
            }, {
              case ((num1, denom1), (num2, denom2)) => (num1 + num2, denom1 + denom2)
            }
          )

        numerator / denominator
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {
    points.find(_._1 == value) match {
      case Some((_, color)) => color
      case None => {
        val (smaller, greater) = points
          .toList
          .sortBy(_._1)
          .partition(_._1 < value)
        if (points.isEmpty) {
          Color(0, 0, 0)
        } else if (smaller.isEmpty) {
          greater.head._2
        } else if (greater.isEmpty) {
          smaller.last._2
        } else {
          val smallNeighbour = smaller.last
          val greatNeighbour = greater.head

          val factor = (value - smallNeighbour._1) / (greatNeighbour._1 - smallNeighbour._1)

          Color(
            round(smallNeighbour._2.red + (greatNeighbour._2.red - smallNeighbour._2.red) * factor).toInt,
            round(smallNeighbour._2.green + (greatNeighbour._2.green - smallNeighbour._2.green) * factor).toInt,
            round(smallNeighbour._2.blue + (greatNeighbour._2.blue - smallNeighbour._2.blue) * factor).toInt
          )
        }
      }
    }
  }

  val width = 360
  val height = 180

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)]): Image = {
    val pixels = (0 until width * height)
      .par
      .map(pos => {
        val color = interpolateColor(colors, predictTemperature(temperatures, pixelToLocation(pos)))
        (pos, RGBColor(color.red, color.green, color.blue, 255).toPixel)
      })
      .toArray
      .sortBy(_._1)
      .map(_._2)
    Image(width, height, pixels)
  }

  /**
    * pixel = x + y * width
    *
    * @param pixel
    */
  def pixelToLocation(pixel: Int): Location = {
    val x = pixel % width
    val y = pixel / width
    val widthScale = 180 * 2 / width.toDouble
    val heightScale = 90 * 2 / height.toDouble
    Location(90 - (y * heightScale), (x * widthScale) - 180)
  }
}

