package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchNewsBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news),
    NewsAdapter.OnItemClickListener {

    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()

        var job: Job? = null
        //TODO: reformat to separate fun
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }

        //TODO: reformat to separate fun
        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages =
                            newsResponse.totalResults / QUERY_PAGE_SIZE + 2 //1 empty page at the end + 1 for INT rounding when dividing
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG)
                            .show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        //TODO: reformat to separate function to define Retry func
        binding.itemErrorMessage.btnRetry.setOnClickListener {
            if (binding.etSearch.text.toString().isNotEmpty()) {
                viewModel.searchNews(binding.etSearch.text.toString())
            } else {
                hideErrorMessage()
            }
        }

    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(this)
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.root.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.text = message
        isError = true
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.root.visibility = View.INVISIBLE
        isError = false
    }


    /**
     * Status helper booleans for *pagination*
     */
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            /** Pagination and View helper vars for better readability
             *
             * With the help of ***layoutManager*** it is possible to calculate
             * if the last item in a single page has been reached.
             *
             * ***isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount***
             */
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBegginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBegginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
                isScrolling = false
            }
        }

        /**
         * Checks if the *View* is currently being scrolled
         */
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    override fun onItemClick(article: Article) {
        val bundle = Bundle().apply {
            putSerializable("article", article)
        }
        //TODO: reformat to separate fun
        binding.root.findNavController().navigate(
            R.id.action_searchNewsFragment_to_articleFragment,
            bundle
        )
    }
}