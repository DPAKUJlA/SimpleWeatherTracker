package com.example.simpleweathertracker

import FiveDayWeatherResponse
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweathertracker.adapter.WeatherAdapter
import com.example.simpleweathertracker.adapter.WeatherDay
import com.example.simpleweathertracker.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cityInput = findViewById<EditText>(R.id.cityInput)
        val getWeatherButton = findViewById<Button>(R.id.getWeatherButton)
        val weatherRecyclerView = findViewById<RecyclerView>(R.id.weatherRecyclerView)

        val currentTemperatureView = findViewById<TextView>(R.id.currentTemperature)
        val currentWeatherDescriptionView = findViewById<TextView>(R.id.currentWeatherDescription)
        val currentWeatherIconView = findViewById<ImageView>(R.id.currentWeatherIcon)
        val locationNameView = findViewById<TextView>(R.id.locationName)
        val currentDateView = findViewById<TextView>(R.id.currentDate) // Новое представление для даты

        sharedPreferences = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)

        loadSavedWeather(
            cityInput,
            locationNameView,
            currentTemperatureView,
            currentWeatherDescriptionView,
            currentWeatherIconView,
            currentDateView
        )

        weatherRecyclerView.layoutManager = LinearLayoutManager(this)

        getWeatherButton.setOnClickListener {
            val city = cityInput.text.toString().trim()

            if (city.isNotEmpty()) {
                fetchWeather(
                    city,
                    weatherRecyclerView,
                    currentTemperatureView,
                    currentWeatherDescriptionView,
                    currentWeatherIconView,
                    locationNameView,
                    currentDateView
                )
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(
        city: String,
        recyclerView: RecyclerView,
        temperatureView: TextView,
        descriptionView: TextView,
        iconView: ImageView,
        locationNameView: TextView,
        currentDateView: TextView
    ) {
        val api = RetrofitClient.api

        api.getFiveDayWeatherByCity(city).enqueue(object : Callback<FiveDayWeatherResponse> {
            override fun onResponse(call: Call<FiveDayWeatherResponse>, response: Response<FiveDayWeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        val cityName = weatherResponse.city.name
                        val country = weatherResponse.city.country
                        locationNameView.text = "$cityName, $country"

                        val currentWeather = weatherResponse.list[0]
                        val currentTemp = currentWeather.main.temp.toInt()
                        val currentDescription = currentWeather.weather[0].description.capitalize()
                        val currentIcon = currentWeather.weather[0].icon
                        val currentDate = formatDate(currentWeather.dt_txt.substring(0, 10))

                        temperatureView.text = "$currentTemp°"
                        descriptionView.text = currentDescription
                        iconView.setImageResource(getIconResource(currentIcon))
                        currentDateView.text = currentDate

                        saveWeather(cityName, country, currentTemp, currentDescription, currentIcon, currentDate)

                        val groupedData = weatherResponse.list.groupBy {
                            it.dt_txt.substring(0, 10)
                        }

                        val weatherData = groupedData.map { (date, weatherList) ->
                            val tempMin = weatherList.minOf { it.main.temp_min }
                            val tempMax = weatherList.maxOf { it.main.temp_max }
                            val icon = weatherList[0].weather[0].icon

                            WeatherDay(
                                day = dateToDay(date),
                                date = formatDate(date),
                                tempMin = tempMin.toInt(),
                                tempMax = tempMax.toInt(),
                                iconResId = getIconResource(icon)
                            )
                        }

                        recyclerView.adapter = WeatherAdapter(weatherData)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Не удалось получить данные", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FiveDayWeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка запроса: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveWeather(
        cityName: String,
        country: String,
        temperature: Int,
        description: String,
        iconCode: String,
        currentDate: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("cityName", cityName)
        editor.putString("country", country)
        editor.putInt("temperature", temperature)
        editor.putString("description", description)
        editor.putString("iconCode", iconCode)
        editor.putString("currentDate", currentDate)
        editor.apply()
    }

    private fun loadSavedWeather(
        cityInput: EditText,
        locationNameView: TextView,
        temperatureView: TextView,
        descriptionView: TextView,
        iconView: ImageView,
        currentDateView: TextView
    ) {
        val cityName = sharedPreferences.getString("cityName", null)
        val country = sharedPreferences.getString("country", null)
        val temperature = sharedPreferences.getInt("temperature", 0)
        val description = sharedPreferences.getString("description", null)
        val iconCode = sharedPreferences.getString("iconCode", null)
        val currentDate = sharedPreferences.getString("currentDate", null)

        if (cityName != null && country != null && description != null && iconCode != null && currentDate != null) {
            cityInput.setText(cityName)
            locationNameView.text = "$cityName, $country"
            temperatureView.text = "$temperature°"
            descriptionView.text = description
            iconView.setImageResource(getIconResource(iconCode))
            currentDateView.text = currentDate
        }
    }

    private fun dateToDay(date: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = sdf.parse(date)
        val dayOfWeekEnglish = SimpleDateFormat("EEEE", Locale.ENGLISH).format(dateObj ?: Date())
        return when (dayOfWeekEnglish) {
            "Monday" -> "Понеділок"
            "Tuesday" -> "Вівторок"
            "Wednesday" -> "Середа"
            "Thursday" -> "Четвер"
            "Friday" -> "П’ятниця"
            "Saturday" -> "Субота"
            "Sunday" -> "Неділя"
            else -> dayOfWeekEnglish
        }
    }

    private fun formatDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("uk"))
        val parsedDate = inputFormat.parse(date)
        return parsedDate?.let { outputFormat.format(it) } ?: date
    }

    private fun getIconResource(iconCode: String): Int {
        return when (iconCode) {
            "01d", "01n" -> R.drawable.ic_sun
            "02d", "02n" -> R.drawable.ic_sun_cloud
            "03d", "03n", "04d", "04n" -> R.drawable.ic_cloud
            "09d", "09n", "10d", "10n" -> R.drawable.ic_rain
            "13d", "13n" -> R.drawable.ic_snow
            else -> R.drawable.ic_cloud
        }
    }
}
