package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSavedNewsBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    private lateinit var binding: FragmentSavedNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //app bar menu
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Saved News"

        binding = FragmentSavedNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()
        loadSavedArticlesFromDB()
        articleItemOnClick()
        moreOptionsMenuListener()

        //ARTICLE SWIPE BEHAVIOUR:
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            //NOT IN USE -> returns true (default)
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            //left and right swipes delete swiped article
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
        //------------------------------------------------------------------------------------
    }

    //APP BAR -> settings button
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.app_bar_settings -> {
                navigateToSettingsFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //------------------------------------------------------------------------------------


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }


    //UI: empty database
    private fun showEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.VISIBLE
    }
    private fun hideEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.INVISIBLE
    }
    //------------------------------------------------------------------------------------


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

    //passes selected article and navigates to ArticleFragment
    private fun articleItemOnClick() {
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            navigateToArticleFragment(bundle)
        }
    }



    private fun moreOptionsMenuListener() {
        newsAdapter.setOnMenuItemClickListener { menuItem, article ->
            when (menuItem.itemId) {
                R.id.menuSave -> {
                    Snackbar.make(binding.root, "Item already saved", Snackbar.LENGTH_SHORT)
                        .show()
                }
                R.id.menuShare -> {
                    viewModel.copyToClipboard(article.url)
                    Toast.makeText(binding.root.context, "URL copied!", Toast.LENGTH_SHORT).show()
                }
                R.id.menuRemove -> {
                    viewModel.deleteArticle(article)
                    Snackbar.make(
                        binding.root,
                        "Successfully deleted article",
                        Snackbar.LENGTH_LONG
                    ).apply {
                        setAction("Undo") {
                            viewModel.saveArticle(article)
                        }
                        show()
                    }
                }
            }
        }
    }

    //FRAGMENT NAVIGATION:
    //bundle is passed via navController
    private fun navigateToArticleFragment(bundle: Bundle) {
        binding.root.findNavController().navigate(
            R.id.action_savedNewsFragment_to_articleFragment,
            bundle
        )
    }
    private fun navigateToSettingsFragment() {
        binding.root.findNavController().navigate(
            R.id.action_savedNewsFragment_to_settingsFragment
        )
    }
    //------------------------------------------------------------------------------------


}