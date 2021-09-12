package com.example.newsapp.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var binding: FragmentArticleBinding
    lateinit var viewModel: NewsViewModel
    private val args: ArticleFragmentArgs by navArgs()
    lateinit var article: Article

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel
        article = args.article

        setRetryButtonClickListener()
        setupWebView()
        setupClickListener(view)
    }


    /**
     * Loads data from [Article] into [WebView]
     */
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    binding.progressBar.visibility = View.VISIBLE
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.visibility = View.INVISIBLE
                    super.onPageFinished(view, url)
                }
            }
            loadUrl(article.url!!)
            checkInternetConnection()
        }
    }

    private fun checkInternetConnection() {
        if (!viewModel.hasInternetConnection()) {
            Toast.makeText(
                binding.root.context,
                "An error occurred: No Internet connection",
                Toast.LENGTH_SHORT
            ).show()
            showErrorMessage()
        } else {
            hideErrorMessage()
        }
    }

    private fun setRetryButtonClickListener() {
        binding.itemErrorMessage.btnRetry.setOnClickListener {
            setupWebView()
        }
    }

    private fun showErrorMessage() {
        binding.itemErrorMessage.root.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.text = "No Internet connection"
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.root.visibility = View.INVISIBLE
    }

    /**
     * Sets ***OnClickListener*** to *Floating Action Button (FAB)*
     *
     * *OnClick* method saves current [Article] to *Database*
     */
    private fun setupClickListener(view: View) {
        binding.fabFavourite.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
        }
    }
}