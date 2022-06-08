package com.example.airpollutionapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.airpollutionapp.R
import com.example.airpollutionapp.fragments.MapsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {


    //    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        var navController = findNavController(R.id.fragmentHJ)

        bottomNavigationView.setupWithNavController(navController)

//        bottomNavigationView = findViewById(R.id.bottomNavigationView)
//        val navController = findNavController(R.id.fragment)
//        val appBarConfiguration = AppBarConfiguration(setOf(R.id.home, R.id.chart))
//
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        bottomNavigationView.setupWithNavController(navController)

    }
}