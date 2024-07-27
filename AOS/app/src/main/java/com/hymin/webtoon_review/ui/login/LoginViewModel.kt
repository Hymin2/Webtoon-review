package com.hymin.webtoon_review.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hymin.webtoon_review.data.dto.request.user.LoginRequest
import com.hymin.webtoon_review.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    var username = MutableLiveData("")
    var password = MutableLiveData("")

    private val _isLoginSuccess = MutableLiveData<Boolean>()
    val isLoginSuccess: LiveData<Boolean> get() = _isLoginSuccess

    private val _isUsernameEmpty = MutableLiveData<Boolean>()
    val isUsernameEmpty: LiveData<Boolean> get() = _isUsernameEmpty

    private val _isPasswordEmpty = MutableLiveData<Boolean>()
    val isPasswordEmpty: LiveData<Boolean> get() = _isPasswordEmpty

    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> get() = _navigateToRegister

    fun onLoginClicked() {
        _isUsernameEmpty.value = username.value.isNullOrEmpty()
        _isPasswordEmpty.value = password.value.isNullOrEmpty()

        if (_isUsernameEmpty.value!! || _isPasswordEmpty.value!!) {
            return
        }

        viewModelScope.launch {
            val response = userRepository.login(LoginRequest(username.value!!, password.value!!))

            if (response.isSuccessful) {
                _isLoginSuccess.value = true

                response.headers()["Authorization"].let {
                    viewModelScope.launch {
                        userRepository.storeJwt(it!!)
                    }
                }
            } else {
                _isLoginSuccess.value = false
            }
        }
    }

    fun onRegisterClicked() {
        _navigateToRegister.value = true
    }

    fun onNavigatedRegister() {
        _navigateToRegister.value = false
    }
}