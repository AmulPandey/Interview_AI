package com.example.interviewai.ui

import androidx.lifecycle.ViewModel
import com.example.interviewai.data.local.OnboardingPreferences
import com.example.interviewai.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val tokenDataStore: TokenDataStore,
    val onboardingPreferences: OnboardingPreferences
) : ViewModel()