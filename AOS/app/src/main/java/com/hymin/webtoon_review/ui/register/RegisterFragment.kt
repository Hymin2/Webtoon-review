package com.hymin.webtoon_review.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.hymin.webtoon_review.R
import com.hymin.webtoon_review.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        viewModel.run {
            isUsernameEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
                when (isEmpty) {
                    true -> binding.registerIdText.error = "아이디를 입력해주세요."
                    false -> binding.registerIdText.error = null
                }
            })

            isPasswordEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
                when (isEmpty) {
                    true -> binding.registerPwText.error = "비밀번호를 입력해주세요."
                    false -> binding.registerPwText.error = null
                }
            })

            isNicknameEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
                when (isEmpty) {
                    true -> binding.registerNicknameText.error = "닉네임을 입력해주세요."
                    false -> binding.registerNicknameText.error = null
                }
            })

            isDuplicatedUsername.observe(viewLifecycleOwner, Observer { isDuplicated ->
                when (isDuplicated) {
                    true -> binding.registerIdText.error = "이미 존재하는 아이디입니다."
                    false -> binding.registerIdText.error = null
                }
            })

            isDuplicatedNickname.observe(viewLifecycleOwner, Observer { isDuplicated ->
                when (isDuplicated) {
                    true -> binding.registerNicknameText.error = "이미 존재하는 닉네임입니다."
                    false -> binding.registerNicknameText.error = null
                }
            })

            isRegisterSuccess.observe(viewLifecycleOwner, Observer { isSuccess ->
                if (isSuccess) {
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
            })
        }
    }

    private fun init() {
        binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            idInput.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    viewModel.onUsernameSubmit()
                }
            }

            nicknameInput.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    viewModel.onNicknameSubmit()
                }
            }

            nicknameInput.setOnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.onNicknameSubmit()
                }
                
                false
            }
        }
    }
}