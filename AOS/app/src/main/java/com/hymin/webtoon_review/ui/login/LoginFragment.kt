package com.hymin.webtoon_review.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hymin.webtoon_review.R
import com.hymin.webtoon_review.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        viewModel.navigateToRegister.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                viewModel.onNavigatedRegister()
            }
        })

        viewModel.run {
            isUsernameEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
                if (isEmpty) {
                    binding.loginIdText.error = "아이디를 입력해주세요."
                } else {
                    binding.loginIdText.error = null
                }
            })

            isPasswordEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
                if (isEmpty) {
                    binding.loginPwText.error = "비밀번호를 입력해주세요."
                } else {
                    binding.loginPwText.error = null
                }
            })

            isLoginSuccess.observe(viewLifecycleOwner, Observer { login ->
                if (login) {
                    findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
                    requireActivity().finish()
                } else {
                    binding.loginPwText.error = "아이디 혹은 비밀번호가 잘못되었습니다."
                }
            })
        }
    }

    private fun init() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }
}