package com.karrar.movieapp.ui.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.map
import com.karrar.movieapp.domain.usecase.GetCategoryUseCase
import com.karrar.movieapp.ui.adapters.MediaInteractionListener
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Constants.FIRST_CATEGORY_ID
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.postEvent
import com.karrar.movieapp.utilities.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    private val mediaUIStateMapper: MediaUIStateMapper,
    private val genreUIStateMapper: GenreUIStateMapper,
    state: SavedStateHandle
) : BaseViewModel(), MediaInteractionListener, CategoryInteractionListener {

    val args = CategoryFragmentArgs.fromSavedStateHandle(state)

    private val _uiState = MutableStateFlow(CategoryUIState())
    val uiState: StateFlow<CategoryUIState> = _uiState.asStateFlow()

    private val _clickMovieEvent = MutableLiveData<Event<Int>>()
    var clickMovieEvent = _clickMovieEvent

    private val _clickRetryEvent = MutableLiveData<Event<Boolean>>()
    val clickRetryEvent = _clickRetryEvent.toLiveData()

    private val _selectedCategory = MutableLiveData(FIRST_CATEGORY_ID)
    val selectedCategory = _selectedCategory.toLiveData()

    init {
        getData()
    }

    override fun getData() {
        getGenre()
        getMediaList()
        _clickRetryEvent.postEvent(true)
    }

    private fun getGenre() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(genre = getCategoryUseCase.getGenreList(args.mediaId).map {
                        genreUIStateMapper.map(it)
                    })
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message.toString()) }
            }
        }
    }

    fun getMediaList() {
        _uiState.update { it.copy(isLoading = true) }
        val result = getCategoryUseCase(args.mediaId, selectedCategory.value ?: FIRST_CATEGORY_ID)

        _uiState.update {
            it.copy(
                isLoading = false,
                media = result.map { pagingData ->
                    pagingData.map { mediaUIStateMapper.map(it) }
                })
        }
    }

    override fun onClickMedia(mediaId: Int) {
        _clickMovieEvent.postValue(Event(mediaId))
    }

    override fun onClickCategory(categoryId: Int) {
        _selectedCategory.postValue(categoryId)
    }

    fun setErrorUiState(loadState: LoadState) {
        when (loadState) {
            is LoadState.Error, null -> {
                _uiState.update { it.copy(error = "error loading") }
            }
            else -> {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

}

