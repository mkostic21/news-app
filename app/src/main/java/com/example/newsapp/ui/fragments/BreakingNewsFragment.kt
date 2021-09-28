package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.snackbar.Snackbar

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //app bar menu
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Breaking News"

        binding = FragmentBreakingNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel

        setupRecyclerView()
        setRetryButtonClickListener()
        setChipsListener()
        handleResponseData()

        articleItemOnClick()
        moreOptionsMenuListener()
    }

    //App bar -> settings button
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


    //booleans for pagination
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    /**
     * Handles *Pagination*, *Response* ***data*** and ***state***
     *
     * *states*: ***Success, Error, Loading***
     */
    private fun handleResponseData() {
        viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    hideEmptyListMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages =
                            newsResponse.totalResults / QUERY_PAGE_SIZE + 2 //2 = 1 empty page at the end + 1 for INT rounding when dividing
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
            viewModel.getBreakingNews()
            hideEmptyListMessage()
            hideErrorMessage()
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
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)

            showEmptyListMessage() //it's empty on init
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
     * With the help of [LinearLayoutManager] it is possible to calculate
     * if the last item in a *response page* has been reached.
     *
     * ***isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount***
     */
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)


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
                viewModel.getBreakingNews()
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
                    viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
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
                            viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
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
            R.id.action_breakingNewsFragment_to_articleFragment,
            bundle
        )
    }

    private fun navigateToSettingsFragment() {
        binding.root.findNavController().navigate(
            R.id.action_breakingNewsFragment_to_settingsFragment
        )
    }

    private fun setChipsListener() {
        binding.apply {
            chipBusiness.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }

            chipEntertainment.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }

            chipGeneral.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory("General")
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                }
            }

            chipHealth.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }

            chipScience.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }

            chipSports.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }

            chipTechnology.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    if (viewModel.category != chip.text.toString()) {
                        viewModel.changeCategory(chip.text.toString())
                        if (viewModel.categoryChanged) {
                            viewModel.getBreakingNews()
                        }
                    }
                } else {
                    chipGeneral.isChecked = true
                    viewModel.changeCategory("General")
                    viewModel.getBreakingNews()
                }
            }
        }
    }

    private fun toggleCheckedCategory() {
        val category = viewModel.category
        binding.apply {
            when (category) {
                "Business" -> chipBusiness.isChecked = true
                "Entertainment" -> chipEntertainment.isChecked = true
                "General" -> chipGeneral.isChecked = true
                "Health" -> chipHealth.isChecked = true
                "Science" -> chipScience.isChecked = true
                "Sports" -> chipSports.isChecked = true
                "Technology" -> chipTechnology.isChecked = true
            }
        }
    }


    override fun onResume() {
        super.onResume()

        //check if settings or category changed -> call refresh with new parameters
        viewModel.loadSettings(context)
        toggleCheckedCategory()
        if (viewModel.countryCodeChanged || viewModel.categoryChanged) {
            viewModel.getBreakingNews()
        }
    }

}
