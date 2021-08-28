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
    private val articleItemListener: OnItemClickListener,
    private val popupMenuListener: OnMenuClickListener
) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root), PopupMenu.OnMenuItemClickListener,
        View.OnClickListener {

        init {
            binding.btnMoreOptions.setOnClickListener {
                val position = adapterPosition
                showPopupMenu(binding, position)
            }
            binding.root.setOnClickListener(this) //sets a click listener to each item in RecyclerView
        }

        /**
         * checking if *clicked* item is **deleted** before the delete animation finishes, with *adapterPosition* value.
         *
         * adapterPosition = **-1** if clicked item is **deleted**
         */
        override fun onClick(v: View?) {
            val position = adapterPosition
            val article = differ.currentList[position]
            if (position != RecyclerView.NO_POSITION) {
                articleItemListener.onItemClick(article)
            }
        }


        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val position = adapterPosition
            val article = differ.currentList[position]
            popupMenuListener.onMenuItemClick(item, article)
            return true
        }
    }


    /**
     * Keeps the **Article** list updated.
     *
     * *Articles* are received from *API*, but *ID* property is used for *local DB*.
     *
     * So we compare *URLs*, which is unique for every *Article*
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
     * displays pop-up menu and passes clicked [MenuItem] and [Article] through [popupMenuListener]
     */
    private fun showPopupMenu(binding: ItemArticlePreviewBinding, position: Int) {
        val popup = PopupMenu(binding.root.context, binding.btnMoreOptions)
        popup.inflate(R.menu.more_options_menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.menuAddToFav -> {
                    popupMenuListener.onMenuItemClick(item, differ.currentList[position])
                }
                R.id.menuShare -> {
                    //TODO: not implemented
                }
                R.id.menuRemove -> {
                    //TODO: not implemented
                }
            }
            true
        }
        popup.show()
    }


    /**
     * Defined custom interface to pass clicked [Article] through [onItemClick]
     */
    interface OnItemClickListener {
        fun onItemClick(article: Article)
    }

    /**
     * Defined custom interface to pass clicked [MenuItem] and [Article]
     */
    interface OnMenuClickListener {
        fun onMenuItemClick(item: MenuItem?, article: Article)
    }


}