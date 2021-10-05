package com.example.newsapp.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.newsapp.NewsApplication
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {
    //breaking news
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null
    var category = "General"
    var categoryChanged = false
    var isRefreshed = false

    //search news
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null
    private var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    //sharedPreferences
    private var countryCode = "us" //default
    var countryCodeChanged = false

    init {
        loadSettings(app.applicationContext)
        getBreakingNews()
    }


    //METHODS FOR API CALLS

    fun refreshBreakingNews(){
        isRefreshed = true
        getBreakingNews()
    }

    fun getBreakingNews() = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)   //emit loading state before making network request
    }


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)    //emit loading state before making network request
    }


    //pagination and RecyclerView List<Article> updating for display
    /**
     * Handles *pagination* and updates *RecyclerView* list of **Articles**
     *
     * (appends new data to existing data in a list)
     */
    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    /**
     *  Updates *RecyclerView* list of **articles**
     *
     * (appends new data to existing data in a list)
     */
    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    //database functions:
    /**
     * Saves an [article] to **database** via [newsRepository]
     */
    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    /**
     * Returns all saved ***Articles*** in **database** via [newsRepository]
     */
    fun getSavedNews() = newsRepository.getSavedNews()

    /**
     * deletes an [article] from **database** via [newsRepository]
     */
    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }


    //internet availability and exception handling functions:
    /**
     * Calls [hasInternetConnection] and depending on internet availability
     * requests a network call via [newsRepository]
     */
    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading()) //post loading state before making a network call

        //if country code changed -> reset data
        if(countryCodeChanged || categoryChanged || isRefreshed){
            breakingNewsPage = 1
            breakingNewsResponse = null
            countryCodeChanged = false
            categoryChanged = false
            isRefreshed = false
        }

        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, category, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No Internet connection..."))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    /**
     * Calls [hasInternetConnection] and depending on internet availability
     * requests a network call via [newsRepository]
     * Handles *pagination* of displayed search results
     */
    private suspend fun safeSearchNewsCall(searchQuery: String) {
        newSearchQuery = searchQuery

        //if a new search query is requested -> reset data
        if(newSearchQuery != oldSearchQuery){
            searchNewsPage = 1
            oldSearchQuery = newSearchQuery
            searchNewsResponse = null
        }
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                //after each query increment page number for next query (paginate)
                val response = newsRepository.searchNews(searchQuery, searchNewsPage++)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No Internet connection..."))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    /**
     * Checks if a **device** is connected to the **Internet**.
     *
     * *Possible connection types*: ***Wi-Fi, Cellular*** *or* ***Ethernet***
     */
    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }


    fun copyToClipboard(text: String?) {
        val clipboard = getApplication<NewsApplication>().getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    fun loadSettings(context: Context?){
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val newCountryCode = sp.getString("country", "us")!!

        //set flag if country code changed
        if(newCountryCode != countryCode){
            countryCode = newCountryCode
            countryCodeChanged = true
        }
    }

    fun changeCategory(newCategory: String) {
        if(newCategory != category){
            category = newCategory
            categoryChanged = true
        }
    }
}