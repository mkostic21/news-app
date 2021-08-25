package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentBreakingNewsBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news),
    NewsAdapter.OnItemClickListener {

    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBreakingNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()
        setRetryButtonClickListener()
        handleResponseData()
    }


    //Application State booleans for pagination
    var isError = false
    var isLoading = false       //TODO: data class
    var isLastPage = false
    var isScrolling = false


    /**
     * Handles *Pagination*, *Response* ***data*** and ***state***
     *
     * *states*: ***Success, Error, Loading***
     */
    private fun handleResponseData() {
        //TODO: Resource responses to separate funcs
        viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages =
                            newsResponse.totalResults / QUERY_PAGE_SIZE + 2 //2 = 1 empty page at the end + 1 for INT rounding when dividing TODO: Separate func
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
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
                }
                is Resource.Loading -> {
                    showProgressBar()
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
            viewModel.getBreakingNews("us") //TODO: programmatic input for countryCode
        }
    }


    /**
     * Sets up a [RecyclerView] *adapter* and passes the
     * custom ***OnClickListener*** in the constructor.
     *
     *  Also adds custom ***OnScrollListener*** defined *below*
     */
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(this)
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }



    // Loading animation and Error screen toggle functions
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        isLoading = true
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
     * With the help of ***layoutManager*** it is possible to calculate
     * if the last item in a *response page* has been reached.
     *
     * ***isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount***
     */
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)


            //TODO: data class + more readable funcs for ifChecks
            //Pagination* and View helper vars for better readability
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            //all defined conditions == true -> paginate
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
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
    override fun onItemClick(article: Article) {
        val bundle = Bundle().apply {
            putSerializable("article", article)
        }

        navigateToArticleFragment(bundle)
    }

    /**
     * Passes [bundle] and navigates to [ArticleFragment] via *NavController*
     */
    private fun navigateToArticleFragment(bundle: Bundle) {
        binding.root.findNavController().navigate(
            R.id.action_breakingNewsFragment_to_articleFragment,
            bundle
        )
    }

}