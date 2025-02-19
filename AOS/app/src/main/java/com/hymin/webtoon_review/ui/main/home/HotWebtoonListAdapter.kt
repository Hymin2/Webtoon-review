package com.hymin.webtoon_review.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hymin.webtoon_review.data.model.home.WebtoonListModel
import com.hymin.webtoon_review.databinding.PagerHotWebtoonListBinding
import com.hymin.webtoon_review.utils.ItemDiffCallBack

class HotWebtoonListAdapter(private val onItemSelected: (webtoon: WebtoonListModel) -> Unit) :
    ListAdapter<WebtoonListModel, HotWebtoonListViewHolder>(ItemDiffCallBack<WebtoonListModel>()) {
    private lateinit var binding: PagerHotWebtoonListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotWebtoonListViewHolder {
        binding =
            PagerHotWebtoonListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return HotWebtoonListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HotWebtoonListViewHolder, position: Int) {
        holder.binding.webtoon = getItem(position)
    }
}

class HotWebtoonListViewHolder(val binding: PagerHotWebtoonListBinding) :
    RecyclerView.ViewHolder(binding.root) {

}