package util

import org.apache.spark.sql.SparkSession

/**
  * Created by horiaradu on 10/04/2017.
  */
trait SparkJob {
  implicit val spark = SparkSession
    .builder
    .appName("Weather app")
    .config("spark.master", "local")
    .getOrCreate
}
