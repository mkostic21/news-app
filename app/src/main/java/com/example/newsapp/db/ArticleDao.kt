package com.example.newsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapp.models.Article

@Dao
interface ArticleDao {

    /**
     * *Inserts* and *replaces* [article] in **database**
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    /**
     * Returns all stored ***Articles*** from **database**
     */
    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Article>>

    /**
     * ***Deletes*** an [article] from **database**
     */
    @Delete
    suspend fun deleteArticle(article: Article)
}