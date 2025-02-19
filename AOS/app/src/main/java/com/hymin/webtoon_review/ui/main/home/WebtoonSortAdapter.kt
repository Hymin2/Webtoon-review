package com.hymin.webtoon_review.ui.main.home

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hymin.webtoon_review.data.model.home.WebtoonSortListModel
import com.hymin.webtoon_review.databinding.RvItemWebtoonSortBinding
import com.hymin.webtoon_review.utils.BaseSelectableAdapter
import com.hymin.webtoon_review.utils.ItemDiffCallBack

class WebtoonSortAdapter(private val onItemSelected: (category: WebtoonSortListModel) -> Unit) :
    BaseSelectableAdapter<WebtoonSortListModel, WebtoonSortViewHolder>(ItemDiffCallBack()) {

    private lateinit var binding: RvItemWebtoonSortBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebtoonSortViewHolder {
        binding =
            RvItemWebtoonSortBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WebtoonSortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WebtoonSortViewHolder, position: Int) {
        holder.apply {
            if (getSelectedPosition() == position) {
                binding.tvSortName.setTypeface(null, Typeface.BOLD);
            } else {
                binding.tvSortName.setTypeface(null, Typeface.NORMAL);
            }

            binding.sort = getItem(position)
            itemView.setOnClickListener {
                selectItem(position, onItemSelected)
            }
        }
    }
}

class WebtoonSortViewHolder(val binding: RvItemWebtoonSortBinding) :
    RecyclerView.ViewHolder(binding.root) {

}