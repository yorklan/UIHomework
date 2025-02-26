package com.york.uihomework

import com.york.uihomework.detail.DetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule
    get() = module {
        viewModel { MainViewModel(get()) }
        viewModel { DetailViewModel(get(), get()) }
    }