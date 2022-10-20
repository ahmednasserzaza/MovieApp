package com.karrar.movieapp.ui.movieDetails.saveMovie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.karrar.movieapp.data.repository.AccountRepository
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.models.CreatedList
import com.karrar.movieapp.ui.UIState
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.checkIfExist
import com.karrar.movieapp.utilities.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SaveMovieViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val accountRepository: AccountRepository,
) : BaseViewModel(), SaveListInteractionListener {

    private val _savedList = MutableLiveData<UIState<List<CreatedList>>>()
    val savedList = _savedList.toLiveData()


    private val _clickListEvent = MutableLiveData<Event<Int>>()
    var clickListEvent: LiveData<Event<Int>> = _clickListEvent

    private val _message = MutableLiveData<String>()
    var message: LiveData<String> = _message

    init {
        getData()
    }

    override fun getData() {
        wrapWithState({
            val sessionId = accountRepository.getSessionId()

            sessionId?.let {
                val response = movieRepository.getAllLists(0, it)
                _savedList.postValue(UIState.Success(response))
            }
        }, { _savedList.postValue(UIState.Error("error")) })
    }

    fun checkMovie(movieId: Int) {
        wrapWithState({
            val result = movieRepository.getListDetails(_clickListEvent.value!!.peekContent())
            if (result.checkIfExist(movieId)) {
                _message.postValue("Fail: this movie is already on the list")
            }
            if (!result.checkIfExist(movieId)) addMovieToList(movieId)
        })
    }


    private fun addMovieToList(movieId: Int) {
        wrapWithState(
            {
                val sessionId = accountRepository.getSessionId()
                sessionId?.let {
                    movieRepository.addMovieToList(
                        sessionId,
                        _clickListEvent.value?.peekContent() ?: 0,
                        movieId
                    )
                    _message.postValue("Susses: The movie has been added")
                }
            })
    }

    override fun onClickList(listId: Int) {
        _clickListEvent.postValue(Event(listId))
    }

}