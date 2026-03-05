package com.example.interviewai.ui.resume

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewai.data.model.GeneratedQuestion
import com.example.interviewai.data.model.ResumeUploadResponse
import com.example.interviewai.data.remote.NetworkResult
import com.example.interviewai.domain.usecase.GetResumeQuestionsUseCase
import com.example.interviewai.domain.usecase.UploadResumeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResumeStep {
    object Upload  : ResumeStep()
    object Parsing : ResumeStep()
    data class Result(val response: ResumeUploadResponse) : ResumeStep()
    data class Questions(
        val questions: List<GeneratedQuestion>,
        val resumeId: String,
        val isLoadingMore: Boolean = false,
        val page: Int = 1
    ) : ResumeStep()
}

data class ResumeUiState(
    val step: ResumeStep = ResumeStep.Upload,
    val selectedFileName: String? = null,
    val selectedFileSize: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val uploadResumeUseCase: UploadResumeUseCase,
    private val getResumeQuestionsUseCase: GetResumeQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeUiState())
    val uiState = _uiState.asStateFlow()

    private var selectedUri: Uri? = null
    private var selectedMime: String = "application/pdf"
    private var selectedName: String = "resume.pdf"
    private var selectedBytes: ByteArray? = null

    fun onFileSelected(uri: Uri, context: Context) {
        selectedUri = uri
        selectedMime = context.contentResolver.getType(uri) ?: "application/pdf"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            selectedName = cursor.getString(nameIdx) ?: "resume.pdf"
            val sizeBytes = cursor.getLong(sizeIdx)
            val sizeStr = when {
                sizeBytes > 1_000_000 -> "%.1f MB".format(sizeBytes / 1_000_000.0)
                else -> "%.0f KB".format(sizeBytes / 1_000.0)
            }
            _uiState.update {
                it.copy(selectedFileName = selectedName, selectedFileSize = sizeStr, error = null)
            }
        }
        selectedBytes = context.contentResolver.openInputStream(uri)?.readBytes()
    }

    fun uploadResume() {
        val bytes = selectedBytes ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, step = ResumeStep.Parsing, error = null) }
            when (val result = uploadResumeUseCase(bytes, selectedName, selectedMime)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, step = ResumeStep.Result(result.data))
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, step = ResumeStep.Upload, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun loadQuestions(resumeId: String) {
        viewModelScope.launch {
            // Stay on Parsing step (don't change — it's already showing)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    // Force parsing step to show spinner
                    step = ResumeStep.Parsing
                )
            }

            when (val result = getResumeQuestionsUseCase(resumeId, count = 10)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = ResumeStep.Questions(
                            questions     = result.data.questions,
                            resumeId      = resumeId,
                            isLoadingMore = false,
                            page          = 1
                        )
                    )
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        step  = ResumeStep.Result(
                            // Go back to result step on error
                            com.example.interviewai.data.model.ResumeUploadResponse(
                                resumeId        = resumeId,
                                fileName        = "",
                                skillsExtracted = emptyList(),
                                experienceYears = 0,
                                jobTitle        = "",
                                projectsFound   = 0,
                                message         = result.message
                            )
                        )
                    )
                }
                else -> {}
            }
        }
    }

    // ─── Load next batch of 10 questions ─────────────────────────────────────
    fun loadMoreQuestions() {
        val currentStep = _uiState.value.step
        if (currentStep !is ResumeStep.Questions) return
        if (currentStep.isLoadingMore) return   // prevent double tap

        viewModelScope.launch {
            // Show loading indicator inside Questions step
            _uiState.update {
                it.copy(
                    step = currentStep.copy(isLoadingMore = true)
                )
            }

            when (val result = getResumeQuestionsUseCase(
                resumeId = currentStep.resumeId,
                count    = 10
            )) {
                is NetworkResult.Success -> {
                    val newQuestions = result.data.questions

                    // Filter out duplicates by question text
                    val existingTexts = currentStep.questions.map { it.text.take(50) }.toSet()
                    val uniqueNew = newQuestions.filter {
                        it.text.take(50) !in existingTexts
                    }

                    _uiState.update {
                        it.copy(
                            step = currentStep.copy(
                                questions     = currentStep.questions + uniqueNew,
                                isLoadingMore = false,
                                page          = currentStep.page + 1
                            )
                        )
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(
                        step  = currentStep.copy(isLoadingMore = false),
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun resetToUpload() {
        _uiState.update { ResumeUiState() }
        selectedBytes = null
        selectedUri   = null
    }
}