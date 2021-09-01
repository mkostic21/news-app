package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSavedNewsBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    private lateinit var binding: FragmentSavedNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSavedNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()
        loadSavedArticlesFromDB()
        articleItemOnClick()
        moreOptionsMenuListener()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            /**
             * This functionality is ***not being used*** so it just ***returns true*** as default behaviour
             */
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            /**
             * On ***left*** or ***right*** *swipe*, deletes selected [Article] from *Database*
             */
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                viewModel.deleteArticle(article)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(article)
                    }
                    show()
                }
                //if database is empty -> show message
                viewModel.getSavedNews().observe(viewLifecycleOwner, { articles ->
                    if (articles.isEmpty()) {
                        showEmptyListMessage()
                    }
                })
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }


    }

    /**
     * Sets up a [RecyclerView] *adapter* and passes the
     * custom ***OnClickListener*** in the constructor.
     */
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    //Empty list message toggle methods
    private fun showEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.VISIBLE
    }

    private fun hideEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.INVISIBLE
    }


    /**
     * Get saved ***Articles*** from ***Database*** and load them into [RecyclerView] *Adapter*
     */
    private fun loadSavedArticlesFromDB() {
        viewModel.getSavedNews().observe(viewLifecycleOwner, { articles ->
            newsAdapter.differ.submitList(articles)

            if (articles.isEmpty()) {
                showEmptyListMessage()
            } else {
                hideEmptyListMessage()
            }
        })
    }

    /**
     * Puts passed [Article] into a [Bundle].
     *
     * Then navigates to [ArticleFragment]
     */
    private fun articleItemOnClick() {
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            navigateToArticleFragment(bundle)
        }
    }


    /**
     * defines each [MenuItem] click functionality
     */
    private fun moreOptionsMenuListener() {
        newsAdapter.setOnMenuItemClickListener { menuItem, _ ->
            when (menuItem.itemId) {
                R.id.menuSave -> {
                    Snackbar.make(binding.root, "Item already saved", Snackbar.LENGTH_SHORT)
                        .show()
                }
                R.id.menuShare -> {
                    //TODO: not implemented yet
                    Toast.makeText(binding.root.context, menuItem.title, Toast.LENGTH_SHORT).show()
                }
                R.id.menuRemove -> {
                    //TODO: not implemented yet
                    Toast.makeText(binding.root.context, menuItem.title, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Passes [bundle] and navigates to [ArticleFragment] via *NavController*
     */
    private fun navigateToArticleFragment(bundle: Bundle) {
        binding.root.findNavController().navigate(
            R.id.action_savedNewsFragment_to_articleFragment,
            bundle
        )
    }


}