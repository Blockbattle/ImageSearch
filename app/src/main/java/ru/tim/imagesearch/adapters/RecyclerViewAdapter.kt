package ru.tim.imagesearch.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.tim.imagesearch.R
import ru.tim.imagesearch.activities.DetailActivity
import ru.tim.imagesearch.models.Image


class RecyclerViewAdapter(
        private val images: ArrayList<Image>,
        private val mContext: Context
) :
    RecyclerView.Adapter<RecyclerViewAdapter.ImageViewHolder>() {

    var onLoadMoreListener: OnLoadMoreListener? = null
    var isLoading: Boolean = false
    var noMore: Boolean = true

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    inner class ImageViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val intent = Intent(mContext, DetailActivity::class.java)
                intent.putExtra("images", images)
                intent.putExtra("position", position)
                intent.putExtra("current", imageView.tag as Int)
                val options: ActivityOptionsCompat = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(mContext as Activity,
                                imageView, imageView.transitionName)
                startActivity(mContext, intent, options.toBundle())
            }
        }
    }

    override fun getItemCount() = images.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)

        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageView = holder.imageView
        imageView.tag = position
        imageView.transitionName = "transition${position}"

        Glide.with(mContext)
            .load(images[position].thumbnail)
            .error(R.drawable.ic_baseline_error)
            .into(imageView)

    }

    override fun onViewAttachedToWindow(holder: ImageViewHolder) {
        super.onViewAttachedToWindow(holder)
        val layoutPosition = holder.layoutPosition
        if (onLoadMoreListener != null && !isLoading && !noMore && layoutPosition == itemCount - 1) {
            isLoading = true
            onLoadMoreListener!!.onLoadMore()
        }
    }
}