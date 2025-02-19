package com.hymin.webtoon_review.ui.main.home.webtoon_list

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hymin.webtoon_review.ui.main.home.HomeViewModel

class DayOfWeeksTabPagerAdapter(
    private val fragment: Fragment,
    private val viewModel: HomeViewModel,
) :
    FragmentStateAdapter(fragment) {
    var fragments: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int = viewModel.tabNames.value!!.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun init() {
        for (i in 1..itemCount) {
            fragments.add(WebtoonListFragment())
        }
    }
}