package com.hymin.webtoon_review.ui.main.home.webtoon_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hymin.webtoon_review.databinding.FragmentWebtoonListBinding
import com.hymin.webtoon_review.ui.main.home.HomeViewModel
import com.hymin.webtoon_review.utils.DisallowParentSwipeOnItemTouchListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebtoonListFragment : Fragment() {
    private var _binding: FragmentWebtoonListBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var genreAdapter: GenreAdapter
    private lateinit var webtoonListAdapter: WebtoonListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentWebtoonListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGenreList()
        setWebtoonList()
    }

    private fun setGenreList() {
        genreAdapter = GenreAdapter { selectedItem ->
            homeViewModel.onGenreItemClicked(selectedItem)
        }

        binding.rvGenre.adapter = genreAdapter
        binding.rvGenre.addOnItemTouchListener(
            DisallowParentSwipeOnItemTouchListener()
        )

        homeViewModel.getGenreList()
        homeViewModel.genre.observe(this.viewLifecycleOwner) {
            genreAdapter.submitList(it)
            genreAdapter.clickFirstItem()
        }
    }

    private fun setWebtoonList() {
        webtoonListAdapter = WebtoonListAdapter { selectedItem ->

        }

        binding.rvWebtoon.adapter = webtoonListAdapter
        binding.rvWebtoon.itemAnimator = null
        homeViewModel.webtoonList.observe(this.viewLifecycleOwner) {
            webtoonListAdapter.submitList(it)
        }
    }
}