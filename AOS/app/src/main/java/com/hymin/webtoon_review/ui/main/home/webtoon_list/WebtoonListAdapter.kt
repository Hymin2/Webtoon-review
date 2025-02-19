package com.hymin.webtoon_review.ui.main.home.webtoon_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hymin.webtoon_review.data.model.home.WebtoonListModel
import com.hymin.webtoon_review.databinding.RvItemWebtoonListBinding
import com.hymin.webtoon_review.utils.ItemDiffCallBack

class WebtoonListAdapter(private val onItemSelected: (webtoon: WebtoonListModel) -> Unit) :
    ListAdapter<WebtoonListModel, WebtoonListViewHolder>(ItemDiffCallBack<WebtoonListModel>()) {

    private lateinit var binding: RvItemWebtoonListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebtoonListViewHolder {
        binding =
            RvItemWebtoonListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WebtoonListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WebtoonListViewHolder, position: Int) {
        holder.binding.webtoon = getItem(position)
    }
}

class WebtoonListViewHolder(val binding: RvItemWebtoonListBinding) :
    RecyclerView.ViewHolder(binding.root) {

}