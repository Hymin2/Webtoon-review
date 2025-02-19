package com.hymin.webtoon_review.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hymin.webtoon_review.databinding.FragmentWebtoonSortBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebtoonSortBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentWebtoonSortBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var webtoonSortAdapter: WebtoonSortAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentWebtoonSortBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClearBtn()
        setSortRv()
    }

    private fun setClearBtn() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setSortRv() {
        webtoonSortAdapter = WebtoonSortAdapter { selectedItem ->
            homeViewModel.onSortItemClicked(selectedItem)
            dismiss()
        }
        binding.rvSort.adapter = webtoonSortAdapter

        homeViewModel.sortList.observe(this.viewLifecycleOwner) {
            webtoonSortAdapter.submitList(it)
        }

        homeViewModel.selectedSort.observe(this.viewLifecycleOwner) {
            webtoonSortAdapter.setSelectedPosition(it.id)
        }
    }
}