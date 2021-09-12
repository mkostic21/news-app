package com.example.newsapp.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityNewsBinding
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.repository.NewsRepository

class NewsActivity : AppCompatActivity(){

    lateinit var binding: ActivityNewsBinding
    lateinit var viewModel: NewsViewModel
    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NewsApp)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)


        /**
         * *navController* needs the **id** of the *host fragment* to successfully initialize.
         * *supportFragmentManager* fetches the **id** of the *navHostFragment* which is then used to build the *navController*
         */
        val hostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment? ?: return
        val navController = hostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController) //bottom navigation

        /**
         * app bar ***back*** button setup
         */
        //list of fragments where back button is NOT displayed
        val fragments = setOf(R.id.breakingNewsFragment, R.id.savedNewsFragment, R.id.searchNewsFragment)
        appBarConfiguration = AppBarConfiguration(fragments)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    //app bar back button
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.newsNavHostFragment).navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.newsNavHostFragment))|| super.onOptionsItemSelected(item)
    }

}