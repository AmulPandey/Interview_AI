package com.example.interviewai.ui.feedback


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor() : ViewModel() {

    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    fun setHasMore(value: Boolean) = _hasMore.update { value }
}