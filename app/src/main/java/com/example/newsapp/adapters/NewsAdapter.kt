package com.example.newsapp.adapters

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemArticlePreviewBinding
import com.example.newsapp.models.Article
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.Exception

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

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
                PopupMenu(binding.root.context, binding.btnMoreOptions).apply {
                    inflate(R.menu.more_options_menu)
                    setOnMenuItemClickListener { menuItem: MenuItem ->
                        when (menuItem.itemId) {
                            R.id.menuSave -> {
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

                    //show menu icons
                    try {
                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                            .apply { isAccessible = true }
                        val mPopup = fieldMPopup.get(this)
                        mPopup.javaClass
                            .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                            .invoke(mPopup, true)
                    } catch (e: Exception) {
                        Log.e("Adapter", "Error showing menu icons", e)
                    } finally {
                        this.show()
                    }
                }
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
        val imgCircularProgressBar = setCircularProgressDrawable(holder.binding.root.context)

        holder.binding.apply {
            Glide.with(root).load(article.urlToImage).placeholder(imgCircularProgressBar)
                .into(ivArticleImage)
            tvSource.text = article.source?.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = getFormattedDate(article.publishedAt, holder.binding.root.context)

        }
    }

    //Placeholder for Glide while loading images
    private fun setCircularProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            setColorSchemeColors(ContextCompat.getColor(context, R.color.darkerBlue))
            start()
        }
    }

    /**
     * @return string: "DAY_OF_WEEK | DAY SHORT-MONTH YEAR"
     */
    private fun getFormattedDate(input: String?, context: Context): String {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val date = LocalDate.parse(input, dateFormat)
        return context.resources.getString(
            R.string.publishedAt,
            date.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }, //from -> to: FRIDAY -> Friday
            date.dayOfMonth,
            date.month.toString().lowercase().replaceFirstChar { it.uppercase() }
                .removeRange(3, date.month.toString().length), //from -> to: SEPTEMBER -> Sep
            date.year
        )
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