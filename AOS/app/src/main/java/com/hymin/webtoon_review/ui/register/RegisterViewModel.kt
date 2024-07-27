package com.hymin.webtoon_review.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hymin.webtoon_review.data.dto.request.user.RegisterRequest
import com.hymin.webtoon_review.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    var username = MutableLiveData("")
    var password = MutableLiveData("")
    var nickname = MutableLiveData("")

    private val _isUsernameEmpty = MutableLiveData<Boolean>()
    val isUsernameEmpty: LiveData<Boolean> get() = _isUsernameEmpty

    private val _isPasswordEmpty = MutableLiveData<Boolean>()
    val isPasswordEmpty: LiveData<Boolean> get() = _isPasswordEmpty

    private val _isNicknameEmpty = MutableLiveData<Boolean>()
    val isNicknameEmpty: LiveData<Boolean> get() = _isNicknameEmpty

    private val _isDuplicatedUsername = MutableLiveData(false)
    val isDuplicatedUsername: LiveData<Boolean> get() = _isDuplicatedUsername

    private val _isDuplicatedNickname = MutableLiveData(false)
    val isDuplicatedNickname: LiveData<Boolean> get() = _isDuplicatedNickname

    private val _isRegisterSuccess = MutableLiveData<Boolean>()
    val isRegisterSuccess: LiveData<Boolean> get() = _isRegisterSuccess

    fun onRegisterClicked() {
        _isUsernameEmpty.value = username.value.isNullOrEmpty()
        _isPasswordEmpty.value = password.value.isNullOrEmpty()
        _isNicknameEmpty.value = nickname.value.isNullOrEmpty()

        if (_isUsernameEmpty.value!! || _isPasswordEmpty.value!! || _isNicknameEmpty.value!! || _isDuplicatedUsername.value!! || _isDuplicatedNickname.value!!) {
            return
        }

        viewModelScope.launch {
            _isRegisterSuccess.value = userRepository.register(
                RegisterRequest(
                    username.value!!,
                    password.value!!,
                    nickname.value!!
                )
            ).isSuccessful
        }
    }

    fun onUsernameSubmit() {
        _isUsernameEmpty.value = username.value.isNullOrEmpty()

        if (_isUsernameEmpty.value!!) {
            return
        }

        viewModelScope.launch {
            val response = userRepository.checkDuplicatedUsername(username.value!!)

            if (response.isSuccessful) {
                _isDuplicatedUsername.value = response.body()!!.data
            }
        }
    }

    fun onNicknameSubmit() {
        _isDuplicatedNickname.value = nickname.value.isNullOrEmpty()

        if (_isDuplicatedNickname.value!!) {
            return
        }

        viewModelScope.launch {
            val response = userRepository.checkDuplicatedNickname(nickname.value!!)

            if (response.isSuccessful) {
                _isDuplicatedNickname.value = response.body()!!.data
            }
        }
    }
}