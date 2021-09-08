package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchNewsBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel
    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()
        setRetryButtonClickListener()
        handleResponseData()
        searchNews()
        articleItemOnClick()
        moreOptionsMenuListener()

    }


    // Status helper booleans for pagination
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    // textChangedListener helper vars
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null


    /**
     * Calls ***ViewModel.SearchNews()*** with a [String] specified in **etSearch**
     *
     * After ***500ms*** if the **etSearch** search bar is **not empty**, it requests the network call with corresponding [String]
     */
    private fun searchNews() {
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        newSearchQuery = editable.toString()

                        //ONLY if a new query is entered, request network call (on back from WebView don't request the same query)
                        if (newSearchQuery != oldSearchQuery) {
                            oldSearchQuery = newSearchQuery
                            viewModel.searchNews(editable.toString())
                        }
                    }
                }
            }
        }
    }


    /**
     * Handles *Pagination*, *Response* ***data*** and ***state***
     *
     * *states*: ***Success, Error, Loading***
     */
    private fun handleResponseData() {
        viewModel.searchNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    hideEmptyListMessage()
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
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG)
                            .show()
                        showErrorMessage(message)
                    }
                    if (isArticleListEmpty()) {
                        showEmptyListMessage()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    if (isArticleListEmpty()) {
                        showEmptyListMessage()
                    }
                }
            }
        })
    }


    /**
     * Sets ***OnClickListener*** to *Retry* button
     *
     * *OnClick* initiates a *network request* via *getBreakingNews()* from ***ViewModel***
     */
    private fun setRetryButtonClickListener() {
        binding.itemErrorMessage.btnRetry.setOnClickListener {
            if (binding.etSearch.text.toString().isNotEmpty()) {
                viewModel.searchNews(binding.etSearch.text.toString())
            } else {
                hideErrorMessage()
                hideEmptyListMessage()
            }
        }
    }


    /**
     * Sets up a [RecyclerView] *adapter* and passes the
     * custom ***OnClickListener*** in the constructor.
     *
     *  Also adds custom ***OnScrollListener*** defined *below*
     */
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)

            showEmptyListMessage() //it's empty on init
        }
    }


    //Loading animation and Error screen toggle functions
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

    private fun showEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.VISIBLE
    }

    private fun hideEmptyListMessage() {
        binding.itemEmptyList.root.visibility = View.INVISIBLE
    }

    private fun isArticleListEmpty(): Boolean {
        return newsAdapter.differ.currentList.isEmpty()
    }


    /**
     * With the help of ***layoutManager*** it is possible to calculate
     * if the last item in a *response page* has been reached.
     *
     * ***isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount***
     */
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //Pagination* and *View* helper vars for better readability
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.searchNews(binding.etSearch.text.toString())
                isScrolling = false


            }
        }

        /**
         * Checks if the [View] is currently being scrolled
         */
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

    }


    /**
     * Puts passed [article] into a [Bundle].
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
     * defines each [MenuItem] functionality
     */
    private fun moreOptionsMenuListener() {
        newsAdapter.setOnMenuItemClickListener { menuItem, article ->
            when (menuItem.itemId) {
                R.id.menuSave -> {
                    viewModel.saveArticle(article)
                    Snackbar.make(binding.root, "Article saved successfully", Snackbar.LENGTH_SHORT)
                        .show()
                }
                R.id.menuShare -> {
                    viewModel.copyToClipboard(article.url)
                    Toast.makeText(binding.root.context, "URL copied!", Toast.LENGTH_SHORT).show()
                }
                R.id.menuRemove -> {
                    val position = newsAdapter.differ.currentList.indexOf(article)

                    //remove article from recycler
                    viewModel.searchNews.observe(viewLifecycleOwner, { response ->
                        response.data?.articles?.remove(article)
                        newsAdapter.differ.submitList(response.data?.articles?.toList())
                    })

                    //restore removed article
                    Snackbar.make(
                        binding.root,
                        "Successfully deleted article",
                        Snackbar.LENGTH_LONG
                    ).apply {
                        setAction("Undo") {
                            viewModel.searchNews.observe(viewLifecycleOwner, { response ->
                                response.data?.articles?.add(position, article)
                                newsAdapter.differ.submitList(response.data?.articles?.toList())
                            })
                        }
                        show()
                    }
                }
            }
        }
    }

    /**
     * Passes [bundle] and navigates to [ArticleFragment] via *NavController*
     */
    private fun navigateToArticleFragment(bundle: Bundle) {
        binding.root.findNavController().navigate(
            R.id.action_searchNewsFragment_to_articleFragment,
            bundle
        )
    }
}