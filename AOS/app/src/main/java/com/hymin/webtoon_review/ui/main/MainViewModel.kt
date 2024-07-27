package com.hymin.webtoon_review.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hymin.webtoon_review.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    fun isExistJwt(): LiveData<Boolean> {
        val existJwt = MutableLiveData<Boolean>()

        viewModelScope.launch {
            existJwt.postValue(userRepository.existJwt())
        }

        return existJwt
    }
}
