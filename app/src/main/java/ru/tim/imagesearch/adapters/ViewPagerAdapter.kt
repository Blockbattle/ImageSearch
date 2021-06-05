package ru.tim.imagesearch.adapters

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.startPostponedEnterTransition
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.tim.imagesearch.R
import ru.tim.imagesearch.models.Image


class ViewPagerAdapter(private val context: Context, private val images: ArrayList<Image>)
    : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(pagerView: View)
        : RecyclerView.ViewHolder(pagerView) {

        val detailImage: ImageView = pagerView.findViewById(R.id.detailImage)
        val imageTitle: TextView = pagerView.findViewById(R.id.imageTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val pagerView = LayoutInflater.from(parent.context)
                .inflate(R.layout.pager, parent, false)

        return PagerViewHolder(pagerView)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val detailImage = holder.detailImage
        val imageTitle = holder.imageTitle

        val image = images[position]

        val thumbnail = Glide.with(context)
                .load(image.thumbnail)
        Glide.with(context)
                .load(image.original)
                .error(R.drawable.ic_baseline_error)
                .thumbnail(thumbnail)
                .into(detailImage)

        imageTitle.text = image.title

        detailImage.tag = position
        detailImage.transitionName = "transition${position}"

        if (position == (context as Activity).intent.getIntExtra("current", 0)) {
            detailImage.viewTreeObserver.addOnPreDrawListener(
                    object : OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            detailImage.viewTreeObserver.removeOnPreDrawListener(this)
                            startPostponedEnterTransition(context)
                            return true
                        }
                    }
            )
        }
    }

    override fun getItemCount() = images.size
}