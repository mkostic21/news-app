package com.example.newsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityNewsBinding

class NewsActivity : AppCompatActivity(){

    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /**
         * navController needs the id of the host fragment to successfully initialize.
         * supportFragmentManager fetches the id of the navHostFragment which is then used to build the navController
         */
        val navController = (supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment).navController

        binding.bottomNavigationView.setupWithNavController(navController)
    }


}