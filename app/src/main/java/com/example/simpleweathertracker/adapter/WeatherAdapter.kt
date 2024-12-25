package com.example.simpleweathertracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleweathertracker.R

data class WeatherDay(
    val day: String,
    val date: String,
    val tempMin: Int,
    val tempMax: Int,
    val iconResId: Int
)

class WeatherAdapter(private val items: List<WeatherDay>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayOfWeek: TextView = view.findViewById(R.id.dayOfWeek)
        val dateText: TextView = view.findViewById(R.id.dateText) //
        val tempMinMax: TextView = view.findViewById(R.id.tempMinMax)
        val weatherIcon: ImageView = view.findViewById(R.id.weatherIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.dayOfWeek.text = item.day
        holder.dateText.text = item.date
        holder.tempMinMax.text = "${item.tempMax}° / ${item.tempMin}°"
        holder.weatherIcon.setImageResource(item.iconResId)
    }

    override fun getItemCount(): Int = items.size
}
