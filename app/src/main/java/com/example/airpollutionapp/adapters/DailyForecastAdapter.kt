package com.example.airpollutionapp.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airpollutionapp.R
import com.example.airpollutionapp.models.DailyForecast


class DailyForecastAdapter : RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    inner class ViewHolder : RecyclerView.ViewHolder {
        var tvTime: TextView
        var tvAqi: TextView

        constructor(itemView: View) : super(itemView) {
            tvTime = itemView.findViewById(R.id.tvDate)
            tvAqi = itemView.findViewById(R.id.tvAqiDaily)
        }
    }

    private var pollutionList: List<DailyForecast>
    private var context: Context

    constructor(pollutionList: List<DailyForecast>, context: Context) : super() {
        this.pollutionList = pollutionList
        this.context = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(
            R.layout.layout_daily_forecast,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var pollution = pollutionList[position]
        var aqi = pollution.aqi!!.toInt()
        when (aqi) {
            1 -> {
                gdCard = GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        intArrayOf(ContextCompat.getColor(context, aqi1Color), ContextCompat.getColor(context, aqi1Color))
                )
                gdCard.setCornerRadius(20f)
                holder.tvAqi.setBackgroundDrawable(gdCard)

            }
            2 -> {
                gdCard = GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        intArrayOf(ContextCompat.getColor(context, aqi2Color), ContextCompat.getColor(context, aqi2Color))
                )
                gdCard.setCornerRadius(20f)
                holder.tvAqi.setBackgroundDrawable(gdCard)
            }
            3 -> {
                gdCard = GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        intArrayOf(ContextCompat.getColor(context, aqi3Color), ContextCompat.getColor(context, aqi3Color))
                )
                gdCard.setCornerRadius(20f)
                holder.tvAqi.setBackgroundDrawable(gdCard)
            }
            4 -> {
                gdCard = GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        intArrayOf(ContextCompat.getColor(context, aqi4Color), ContextCompat.getColor(context, aqi4Color))
                )
                gdCard.setCornerRadius(20f)
                holder.tvAqi.setBackgroundDrawable(gdCard)
            }
            5 -> {
                gdCard = GradientDrawable(
                        GradientDrawable.Orientation.RIGHT_LEFT,
                        intArrayOf(ContextCompat.getColor(context, aqi5Color), ContextCompat.getColor(context, aqi5Color))
                )
                gdCard.setCornerRadius(20f)
                holder.tvAqi.setBackgroundDrawable(gdCard)
            }
            else -> {
                holder.tvAqi.setBackgroundResource(R.drawable.background_recycler_view_time)
            }
        }
        holder.tvAqi.text = pollution.aqi!!.toInt().toString()
        holder.tvTime.text = pollution.dt!!.toString()
    }

    override fun getItemCount(): Int {
        return pollutionList.size
    }

    companion object {
        private lateinit var gdCard: GradientDrawable
        private var aqi1Color = R.color.aqi1_1
        private var aqi2Color = R.color.aqi2
        private var aqi3Color = R.color.aqi3
        private var aqi4Color = R.color.aqi4
        private var aqi5Color = R.color.aqi5
    }
}