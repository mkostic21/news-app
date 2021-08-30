package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemArticlePreviewBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class NewsAdapter(
) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            var article: Article

            // listener for each article item in recycler view
            binding.root.setOnClickListener {
                article = differ.currentList[adapterPosition]
                onItemClickListener?.let { it(article) }
            }

            //popup menu button and menu-item listeners
            binding.btnMoreOptions.setOnClickListener {
                article = differ.currentList[adapterPosition]
                val popup = PopupMenu(binding.root.context, binding.btnMoreOptions).apply {
                    inflate(R.menu.more_options_menu)
                    setOnMenuItemClickListener { menuItem: MenuItem ->
                        when (menuItem.itemId) {
                            R.id.menuAddToFav -> {
                                onMenuItemClickListener?.let { it(menuItem, article) }
                            }
                            R.id.menuShare -> {
                                onMenuItemClickListener?.let { it(menuItem, article) }
                            }
                            R.id.menuRemove -> {
                                onMenuItemClickListener?.let { it(menuItem, article) }
                            }
                        }
                        true
                    }
                }
                popup.show()
            }

        }

    }


    /**
     * Keeps the [Article] list updated.
     *
     * *Articles* are received from *API*, *ID* is used for *local DB*.
     *
     * Compares *URLs*, which is unique for every [Article]
     */
    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ItemArticlePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]

        holder.binding.apply {
            Glide.with(root).load(article.urlToImage).into(ivArticleImage)
            tvSource.text = article.source?.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    /**
     * Custom listener to pass clicked [Article]
     */
    private var onItemClickListener: ((Article) -> Unit)? = null
    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

    /**
     * custom listener passes [MenuItem] and [Article]
     */
    private var onMenuItemClickListener: ((MenuItem, Article) -> Unit)? = null
    fun setOnMenuItemClickListener(listener: (MenuItem, Article) -> Unit) {
        onMenuItemClickListener = listener
    }


}