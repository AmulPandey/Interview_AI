package com.example.interviewai.ui.interview


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.data.model.*
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.domain.usecase.AnalyzeAnswerUseCase
import com.example.interviewai.domain.usecase.GenerateQuestionsUseCase
import com.example.interviewai.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewUiState(
    val questions: List<GeneratedQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answerText: String = "",
    val isListening: Boolean = false,
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val result: AnalysisResult? = null,
    val elapsedSeconds: Int = 0,
    val partialSpeech: String = ""
)

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val generateQuestionsUseCase: GenerateQuestionsUseCase,
    private val analyzeAnswerUseCase: AnalyzeAnswerUseCase,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var timerJob: Job? = null
    private var userId = "" // replace with real auth

    init {
        loadUserId()                                    // ← load real userId on start
    }

    private fun loadUserId() {
        viewModelScope.launch {
            tokenDataStore.userId.firstOrNull()?.let { id ->
                userId = id
                println(">>> InterviewViewModel userId: $userId")
            }
        }
    }

    fun loadQuestions(category: String, difficulty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = getQuestionsUseCase(category, difficulty)) {
                is NetworkResult.Success -> {
                    val questions = result.data.questions
                    if (questions.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No questions found for this category and difficulty.",
                                questions = emptyList(),
                                currentIndex = 0
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                questions = questions,
                                currentIndex = 0,
                                isLoading = false,
                                error = null,
                                answerText = "",
                                result = null,
                                elapsedSeconds = 0
                            )
                        }
                        startTimer()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load questions",
                            questions = emptyList(),
                            currentIndex = 0
                        )
                    }
                }

                else -> {}
            }
        }
    }
    // ─── Timer ─────────────────────────────────────────────
    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _uiState.update { it.copy(elapsedSeconds = 0) }
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // ─── Speech-to-Text ────────────────────────────────────
    fun startListening(context: Context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?)  = Unit
                override fun onBeginningOfSpeech()         = Unit
                override fun onRmsChanged(p: Float)        = Unit
                override fun onBufferReceived(p: ByteArray?) = Unit
                override fun onEndOfSpeech()               = Unit

                override fun onPartialResults(results: Bundle?) {
                    val partial = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    _uiState.update { it.copy(partialSpeech = partial) }
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    _uiState.update {
                        it.copy(
                            answerText   = (it.answerText + " " + text).trim(),
                            isListening  = false,
                            partialSpeech = ""
                        )
                    }
                }

                override fun onError(error: Int) {
                    _uiState.update { it.copy(isListening = false, partialSpeech = "") }
                }

                override fun onEvent(p: Int, p1: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        _uiState.update { it.copy(isListening = true) }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.update { it.copy(isListening = false) }
    }

    // ─── Answer Editing ────────────────────────────────────
    fun updateAnswerText(text: String) {
        _uiState.update { it.copy(answerText = text) }
    }

    // ─── Submit ────────────────────────────────────────────
    fun submitAnswer() {
        val currentState    = _uiState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentIndex) ?: return
        if (currentState.isAnalyzing) return

        stopTimer()

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }

            val request = AnswerRequest(
                questionId       = currentQuestion.id
                    ?.takeIf { it.isNotBlank() }
                    ?: currentQuestion.text.hashCode().toString(), // ← safe fallback
                answerText       = currentState.answerText.trim(),
                userId           = userId,
                durationSeconds  = currentState.elapsedSeconds.coerceAtLeast(0),
                questionText     = currentQuestion.text,
                sampleAnswer     = currentQuestion.sampleAnswer ?: "",
                expectedKeywords = currentQuestion.expectedKeywords ?: emptyList(),
                category         = currentQuestion.category ?: ""
            )

            when (val result = analyzeAnswerUseCase(request)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(result = result.data, isAnalyzing = false)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(error = result.message ?: "Failed to analyze", isAnalyzing = false)
                }
                else -> {}
            }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex + 1 >= state.questions.size) {
            // Optional: handle end of questions – go to summary / results screen
            _uiState.update { it.copy(error = "No more questions!") }
            return
        }

        stopTimer()

        _uiState.update {
            it.copy(
                currentIndex   = it.currentIndex + 1,
                answerText     = "",
                partialSpeech  = "",
                result         = null,
                elapsedSeconds = 0,
                isListening    = false
            )
        }

        startTimer()
    }


    fun loadSingleQuestion(question: GeneratedQuestion) {
        stopTimer()

        _uiState.update {
            it.copy(
                questions = listOf(question),
                currentIndex = 0,
                answerText = "",
                partialSpeech = "",
                result = null,
                elapsedSeconds = 0,
                isListening = false,
                isLoading = false,
                error = null
            )
        }

        startTimer()
    }
    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        timerJob?.cancel()
    }
}