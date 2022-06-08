package com.example.airpollutionapp.fragments

import androidx.fragment.app.Fragment
import android.view.View
import com.example.airpollutionapp.R
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_recommendations.*

class RecommendationsFragment : Fragment(R.layout.fragment_recommendations) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val gdaqi1 = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            intArrayOf(resources.getColor(R.color.aqi1_1), resources.getColor(R.color.aqi1_1))
        )
        gdaqi1.setCornerRadius(20f)
        aqi1R.setBackgroundDrawable(gdaqi1)
        tvR1.text = reccomendations[0]


        val gdaqi2 = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            intArrayOf(resources.getColor(R.color.aqi2), resources.getColor(R.color.aqi2))
        )
        gdaqi2.setCornerRadius(20f)
        aqi2R.setBackgroundDrawable(gdaqi2)
        tvR2.text = reccomendations[1]

        val gdaqi3 = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            intArrayOf(resources.getColor(R.color.aqi3), resources.getColor(R.color.aqi3))
        )
        gdaqi3.setCornerRadius(20f)
        aqi3R.setBackgroundDrawable(gdaqi3)
        tvR3.text = reccomendations[2]

        val gdaqi4 = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            intArrayOf(resources.getColor(R.color.aqi4), resources.getColor(R.color.aqi4))
        )
        gdaqi4.setCornerRadius(20f)
        aqi4R.setBackgroundDrawable(gdaqi4)
        tvR4.text = reccomendations[3]

        val gdaqi5 = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT,
            intArrayOf(resources.getColor(R.color.aqi5), resources.getColor(R.color.aqi5))
        )
        gdaqi5.setCornerRadius(20f)
        aqi5R.setBackgroundDrawable(gdaqi5)
        tvR5.text = reccomendations[4]

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        private var reccomendations = arrayOf("Enjoy your usual outdoor activities.",
                "No need to modify your usual outdoor activities unless you experience symptoms such as coughing and throat irritation.",
                "Some pollutants may slightly affect very few hypersensitive individuals.",
                "Consider reducing or rescheduling strenuous activities outdoors if you experience symptoms such as coughing and throat irritation.",
                "Avoid strenuous activities outdoors. Children and the elderly should also avoid outdoor physical exertion.")

    }
}