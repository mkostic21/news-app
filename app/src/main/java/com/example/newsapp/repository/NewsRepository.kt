package com.example.newsapp.repository

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.models.Article

class NewsRepository(
    private val db: ArticleDatabase
) {
    /**
     * Does a [RetrofitInstance.api] Call
     */
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    /**
     * Does a [RetrofitInstance.api] Call
     */
    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    /**
     * *Inserts* and *replaces* [article] in [db]
     */
    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    /**
     * Returns all stored ***Articles*** from [db]
     */
    fun getSavedNews() = db.getArticleDao().getAllArticles()

    /**
     * ***Deletes*** an [article] from [db]
     */
    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
}