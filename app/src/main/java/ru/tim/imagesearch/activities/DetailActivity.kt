package ru.tim.imagesearch.activities

import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tim.imagesearch.R
import ru.tim.imagesearch.adapters.ViewPagerAdapter
import ru.tim.imagesearch.models.Image

class DetailActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        setContentView(R.layout.image_detail)

        val images = intent.getParcelableArrayListExtra<Image>("images")
        val position = intent.getIntExtra("position", 0)

        viewPager = findViewById(R.id.viewpager)
        val pagerAdapter = ViewPagerAdapter(this, images)
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(position, false)

        val webButton: FloatingActionButton = findViewById(R.id.webButton1)
        webButton.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra("link", images[viewPager.currentItem].link)
            ContextCompat.startActivity(this, intent, null)
        }
    }

    override fun finishAfterTransition() {
        val position = viewPager.currentItem
        val intent2 = Intent()
        intent2.putExtra("exit_position", position)
        setResult(RESULT_OK, intent2)

        if(intent.getIntExtra("current", 0) != position) {
            val view: View = viewPager.findViewWithTag(position)

            setEnterSharedElementCallback(object : SharedElementCallback() {
                override fun onMapSharedElements(
                        names: MutableList<String?>, sharedElements: MutableMap<String?, View?>) {
                    names.clear()
                    sharedElements.clear()
                    names.add(view.transitionName)
                    sharedElements[view.transitionName] = view
                }
            })
        }
        super.finishAfterTransition()
    }
}