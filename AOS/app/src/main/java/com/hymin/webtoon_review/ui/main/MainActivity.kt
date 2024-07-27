package com.hymin.webtoon_review.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.hymin.webtoon_review.databinding.ActivityMainBinding
import com.hymin.webtoon_review.ui.intro.IntroActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        viewModel.isExistJwt().observe(this, Observer { exist ->
            if (!exist) {
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        })
    }

    private fun init() {
        binding.vm = viewModel
        binding.lifecycleOwner = this
    }
}