package observatory

case class Location(lat: Double, lon: Double)

case class Color(red: Int, green: Int, blue: Int)

case class Station(id: String, latitude: Double, longitude: Double)

case class TemperatureRecord(id: String, day: Int, month: Int, year: Int, temperature: Double)