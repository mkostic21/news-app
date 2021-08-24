package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
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
    val args: ArticleFragmentArgs by navArgs()
    lateinit var article: Article

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel
        article = args.article

        setupWebView()
        setupClickListener(view)
    }


    /**
     * Loads data from ***Article*** into *WebView*
     */
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url!!)
        }
    }

    /**
     * Sets ***OnClickListener*** to *Floating Action Button (FAB)*
     *
     * *OnClick* method saves current ***Article*** to *Database*
     */
    private fun setupClickListener(view: View) {
        binding.fabFavourite.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
        }
    }
}