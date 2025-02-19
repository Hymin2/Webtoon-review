package com.hymin.webtoon_review.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hymin.webtoon_review.R
import com.hymin.webtoon_review.databinding.FragmentHomeBinding
import com.hymin.webtoon_review.ui.main.home.webtoon_list.DayOfWeeksTabPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var hotWebtoonListAdapter: HotWebtoonListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDayOfWeeksTab()
        setSortButton()
        setHotWebtoonList()

        homeViewModel.combinedTrigger.observe(this.viewLifecycleOwner) {

        }
    }

    private fun setHotWebtoonList() {
        hotWebtoonListAdapter = HotWebtoonListAdapter { selectedItem ->

        }

        homeViewModel.getHotWebtoonList()
        binding.viewPagerHotWebtoon.adapter = hotWebtoonListAdapter
        homeViewModel.hotWebtoonList.observe(this.viewLifecycleOwner) {
            hotWebtoonListAdapter.submitList(it)
        }
    }

    private fun setSortButton() {
        binding.btnSort.setOnClickListener {
            val webtoonSortBottomSheetFragment = WebtoonSortBottomSheetFragment().apply {
                setStyle(
                    BottomSheetDialogFragment.STYLE_NORMAL,
                    R.style.AppBottomSheetDialogBorder20WhiteTheme
                )
            }
            webtoonSortBottomSheetFragment.show(
                parentFragmentManager,
                webtoonSortBottomSheetFragment.tag
            )
        }

        homeViewModel.selectedSort.observe(this.viewLifecycleOwner) {
            binding.btnSort.text = it.name
        }
    }

    private fun setDayOfWeeksTab() {
        val adapter = DayOfWeeksTabPagerAdapter(this, homeViewModel)

        binding.apply {
            pagerDayOfWeek.adapter = adapter
            adapter.init()

            homeViewModel.tabNames.observe(this@HomeFragment.viewLifecycleOwner) {
                TabLayoutMediator(tabDayOfWeek, pagerDayOfWeek) { tab, position ->
                    tab.text = it[position]
                }.attach()
            }

            tabDayOfWeek.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab!!.position.let {
                        homeViewModel.onDayOfWeekItemClicked(it)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
        }
    }
}