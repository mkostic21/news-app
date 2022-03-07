package com.example.newsapp.api

import android.app.Application
import android.content.Context
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants.Companion.BASE_URL
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.coroutines.coroutineContext

class RetrofitInstance {

    companion object {

        private lateinit var cacheDir: File

        //retrofit Singleton
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            //caching
            val cacheSize = (5 * 1024 * 1024).toLong() //5MB
            val cache = Cache(cacheDir, cacheSize)

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val result = request.newBuilder().header("Cache-Control", "public, max-age="+ 60 * 5).build() //if cached data is older than 5min, request new call, otherwise show cached data
                    chain.proceed(result)
                }
                .build()
//-------------------------------------------------------------------------------------


            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api by lazy {
            retrofit.create(NewsAPI::class.java)
        }

        //for cache dir, context is needed -> it's being set on app launch(MAIN)
        fun setCacheDir(cacheDir: File){
            this.cacheDir = cacheDir
        }
    }
}