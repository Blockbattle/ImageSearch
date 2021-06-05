package ru.tim.imagesearch.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import ru.tim.imagesearch.R


class WebActivity : AppCompatActivity(){
    private lateinit var webView: WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webpage)
        webView = findViewById(R.id.webview)
        webView.apply {
            webViewClient = WebViewClient()
            loadUrl(intent.getStringExtra("link"))
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }
}