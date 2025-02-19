package com.hymin.webtoon_review.ui.main.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hymin.webtoon_review.data.model.home.WebtoonGenreModel
import com.hymin.webtoon_review.data.model.home.WebtoonListModel
import com.hymin.webtoon_review.data.model.home.WebtoonSortListModel
import com.hymin.webtoon_review.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _tabNames = MutableLiveData<List<String>>()
    val tabNames: LiveData<List<String>> get() = _tabNames

    private val _sortList = MutableLiveData<List<WebtoonSortListModel>>()
    val sortList: LiveData<List<WebtoonSortListModel>> get() = _sortList

    private val _genre = MutableLiveData<List<WebtoonGenreModel>>()
    val genre: LiveData<List<WebtoonGenreModel>> get() = _genre

    private val _hotWebtoonList = MutableLiveData<List<WebtoonListModel>>()
    val hotWebtoonList: LiveData<List<WebtoonListModel>> get() = _hotWebtoonList

    private val _webtoonList = MutableLiveData<List<WebtoonListModel>>()
    val webtoonList: LiveData<List<WebtoonListModel>> get() = _webtoonList

    private val _selectedGenre = MutableLiveData<WebtoonGenreModel>()
    val selectedGenre: LiveData<WebtoonGenreModel> get() = _selectedGenre

    private val _selectedDayOfWeek = MutableLiveData<String>()
    val selectedDayOfWeek: LiveData<String> get() = _selectedDayOfWeek

    private val _selectedSort = MutableLiveData<WebtoonSortListModel>()
    val selectedSort: LiveData<WebtoonSortListModel> get() = _selectedSort

    val combinedTrigger = MediatorLiveData<Unit>()

    private var isInitialized = false

    init {
        _tabNames.value = listOf("전체", "월", "화", "수", "목", "금", "토", "일")
        _sortList.value = listOf(
            WebtoonSortListModel(0, "인기순"),
            WebtoonSortListModel(1, "남성 인기순"),
            WebtoonSortListModel(2, "여성 인기순"),
            WebtoonSortListModel(3, "별점순"),
            WebtoonSortListModel(4, "추천순"),
            WebtoonSortListModel(5, "최신순")
        )
        _selectedDayOfWeek.value = tabNames.value!![0]
        _selectedSort.value = sortList.value!![0]

        combinedTrigger.addSource(_selectedGenre) { if (isInitialized) getWebtoonList("genre") }
        combinedTrigger.addSource(_selectedDayOfWeek) { if (isInitialized) getWebtoonList("day of week") }
        combinedTrigger.addSource(_selectedSort) { if (isInitialized) getWebtoonList("sort") }
    }

    fun getGenreList() {
        Log.d("called func", "getGenreList()")
        viewModelScope.launch {
            val genreList = mutableListOf<WebtoonGenreModel>()
            genreList.add(WebtoonGenreModel(0, "전체"))
            genreList.addAll(homeRepository.getGenreList())
            _genre.value = genreList.toList()
        }
    }

    fun getWebtoonList(str: String) {
        Log.d("called func", "getWebtoonList(${str}), $isInitialized")
        viewModelScope.launch {
            val dayOfWeek = if (selectedDayOfWeek.value == "전체") null else selectedDayOfWeek.value
            val genre = if (selectedGenre.value?.name == "전체") null else selectedGenre.value?.name

            _webtoonList.value = homeRepository.getWebtoonList(
                selectedSort.value!!.name,
                dayOfWeek,
                genre,
                null,
                null
            )
        }
    }

    fun getHotWebtoonList() {
        viewModelScope.launch {
            _hotWebtoonList.value = homeRepository.getHotWebtoonList()
        }
    }

    fun onDayOfWeekItemClicked(position: Int) {
        Log.d("called func", "onDayOfWeekItemClicked()")
        _selectedDayOfWeek.value = tabNames.value!![position]
    }

    fun onGenreItemClicked(item: WebtoonGenreModel) {
        Log.d("called func", "onGenreItemClicked()")
        isInitialized = true
        _selectedGenre.value = item
    }

    fun onSortItemClicked(item: WebtoonSortListModel) {
        Log.d("called func", "onSortItemClicked()")
        _selectedSort.value = item
    }
}