package com.hymin.webtoon_review.ui.main.home.webtoon_list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hymin.webtoon_review.R
import com.hymin.webtoon_review.data.model.home.WebtoonGenreModel
import com.hymin.webtoon_review.databinding.RvItemWebtoonGenreBinding
import com.hymin.webtoon_review.utils.ItemDiffCallBack

class GenreAdapter(private val onItemSelected: (category: WebtoonGenreModel) -> Unit) :
    ListAdapter<WebtoonGenreModel, GenreViewHolder>(ItemDiffCallBack<WebtoonGenreModel>()) {

    private lateinit var binding: RvItemWebtoonGenreBinding
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        binding =
            RvItemWebtoonGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.binding.genre = getItem(position)

        if (selectedPosition == position) {
            holder.binding.tvGenre.setBackgroundResource(R.drawable.rectangle_selected_category_style)
            holder.binding.tvGenre.setTextColor(Color.WHITE)
        } else {
            holder.binding.tvGenre.setBackgroundResource(R.drawable.rectangle_category_style)
            holder.binding.tvGenre.setTextColor(Color.BLACK)
        }

        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(position)
            onItemSelected(getItem(position))
        }
    }

    fun clickFirstItem() {
        onItemSelected(getItem(0))
    }
}

class GenreViewHolder(val binding: RvItemWebtoonGenreBinding) :
    RecyclerView.ViewHolder(binding.root) {

}