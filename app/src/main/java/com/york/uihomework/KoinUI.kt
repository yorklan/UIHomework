package com.york.uihomework

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule
    get() = module {
        viewModel { MainViewModel(get()) }
    }