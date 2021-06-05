package ru.tim.imagesearch.activities

import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import ru.tim.imagesearch.R
import ru.tim.imagesearch.adapters.RecyclerViewAdapter
import ru.tim.imagesearch.decorations.ItemDecoration
import ru.tim.imagesearch.models.Image
import serpapi.GoogleSearch
import serpapi.SerpApiHttpClient
import serpapi.SerpApiSearchException


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var recyclerView: RecyclerView
    private val images = ArrayList<Image>()
    private val adapter = RecyclerViewAdapter(images, this)
    private var thread: Thread? = null

    private var currentQuery: String? = null
    private var currentPage: Int = 0

    private var exitPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            images.addAll(savedInstanceState.getParcelableArrayList<Image>("image_list")!!)
            adapter.noMore = savedInstanceState.getBoolean("no_more", true)
            currentPage = savedInstanceState.getInt("page", 0)
            currentQuery = savedInstanceState.getString("query")
        }

        setContentView(R.layout.activity_main)

        val spanCount = 3
        val spacingDp = 3f
        val spacingPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                spacingDp,
                this.resources.displayMetrics
        ).toInt()

        recyclerView = findViewById(R.id.recyclerView)

        val gridLayoutManager = GridLayoutManager(this, spanCount)
        recyclerView.layoutManager = gridLayoutManager

        recyclerView.addItemDecoration(ItemDecoration(spanCount, spacingPx))

        class OnLoadMoreListener : RecyclerViewAdapter.OnLoadMoreListener {
            override fun onLoadMore() {
                imageRequest()
                adapter.isLoading = false
            }
        }

        adapter.onLoadMoreListener = OnLoadMoreListener()

        recyclerView.adapter = adapter
        if (currentQuery != null && currentPage == 0)
            imageRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("image_list", images)
        outState.putBoolean("no_more", adapter.noMore)
        outState.putString("query", currentQuery)
        outState.putInt("page", currentPage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        adapter.isLoading = true
        currentQuery = query
        currentPage = 0
        adapter.noMore = false

        if (thread != null) {
            val dummy = thread
            thread = null
            dummy!!.interrupt()
        }

        imageRequest()

        adapter.isLoading = false
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun imageRequest() {
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        thread = Thread {
            SerpApiHttpClient.BACKEND = "https://serpapi.com/"
            val parameter: MutableMap<String, String> = HashMap()

            parameter["q"] = currentQuery!!
            parameter["tbm"] = "isch"
            parameter["ijn"] = currentPage.toString()
            parameter[GoogleSearch.SERP_API_KEY_NAME] = getString(R.string.api_key)

            val search = GoogleSearch(parameter)

            try {
                val results: JsonObject = search.json
                val imagesResults = results.get("images_results")

                val newImages = ArrayList<Image>()

                if (imagesResults == null) {
                    adapter.noMore = true
                }
                else {
                    val jsonImages: JsonArray = imagesResults.asJsonArray
                    jsonImages.forEach { i ->
                        newImages.add(Gson().fromJson(i, Image::class.java))
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    progressBar.visibility = View.INVISIBLE

                    if (currentPage == 0) {
                        recyclerView.scrollToPosition(0)
                        images.clear()
                    }
                    images.addAll(newImages)
                    adapter.notifyDataSetChanged()
                    currentPage += 1
                }
            } catch (e: SerpApiSearchException) {
                e.printStackTrace()
            }
            catch (e: JsonParseException) {
                e.printStackTrace()
            }
        }

        thread!!.start()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            exitPosition = data.getIntExtra("exit_position", 0)
            val layoutManager = recyclerView.layoutManager
            val viewAtPosition: View? = layoutManager!!.findViewByPosition(exitPosition)

            if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition,
                            false, true)) {
                layoutManager.scrollToPosition(exitPosition)
                setTransitionOnView()
            } else {
                setTransitionOnView()
            }
        }
    }

    private class CustomSharedElementCallback : SharedElementCallback() {
        var mView: View? = null

        override fun onMapSharedElements(
                names: MutableList<String?>, sharedElements: MutableMap<String?, View?>) {
            names.clear()
            sharedElements.clear()
            if (mView != null) {
                val transitionName = ViewCompat.getTransitionName(mView!!)
                names.add(transitionName)
                sharedElements[transitionName] = mView
            }
        }
    }

    private fun setTransitionOnView() {
        val callback = CustomSharedElementCallback()
        setExitSharedElementCallback(callback)
        window.sharedElementExitTransition.addListener(object : Transition.TransitionListener {

            override fun onTransitionStart(p0: Transition?) {
            }

            override fun onTransitionEnd(p0: Transition?) {
                removeCallback()
            }

            override fun onTransitionCancel(p0: Transition?) {
                removeCallback()
            }

            override fun onTransitionPause(p0: Transition?) {
            }

            override fun onTransitionResume(p0: Transition?) {
            }

            private fun removeCallback() {
                window.sharedElementExitTransition.removeListener(this)
                setExitSharedElementCallback(null as SharedElementCallback?)
            }
        })
        postponeEnterTransition()
        recyclerView.viewTreeObserver
                .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                        val holder = recyclerView.findViewHolderForAdapterPosition(exitPosition)
                        if (holder is RecyclerViewAdapter.ImageViewHolder) {
                            callback.mView = holder.imageView
                        }
                        startPostponedEnterTransition()
                        return true
                    }
                })
    }
}