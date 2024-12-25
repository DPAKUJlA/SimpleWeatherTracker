data class FiveDayWeatherResponse(
    val city: City,
    val list: List<WeatherData>
)

data class WeatherData(
    val main: MainWeather,
    val weather: List<WeatherDescription>,
    val dt_txt: String
)

data class MainWeather(
    val temp: Float,
    val temp_min: Float,
    val temp_max: Float
)

data class WeatherDescription(
    val description: String,
    val icon: String
)

data class City(
    val name: String,
    val country: String
)