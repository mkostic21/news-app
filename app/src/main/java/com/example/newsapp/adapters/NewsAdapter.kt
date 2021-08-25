package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.databinding.ItemArticlePreviewBinding
import com.example.newsapp.models.Article

class NewsAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {
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
                listener.onItemClick(article)
            }
        }
    }

    /**
     * Defined custom *OnClickInterface* to pass clicks to other **Views**
     */
    interface OnItemClickListener {
        fun onItemClick(article: Article)
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

    /**
     * Load data from **Article** to corresponding fields in the layout.
     */
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

}