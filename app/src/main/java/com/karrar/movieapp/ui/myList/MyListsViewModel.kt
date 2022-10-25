package com.karrar.movieapp.ui.myList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.karrar.movieapp.data.repository.AccountRepository
import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.models.CreatedList
import com.karrar.movieapp.ui.base.BaseViewModel
import com.karrar.movieapp.ui.movieDetails.saveMovie.SaveListInteractionListener
import com.karrar.movieapp.utilities.Event
import com.karrar.movieapp.utilities.checkIfExist
import com.karrar.movieapp.utilities.postEvent
import com.karrar.movieapp.utilities.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyListsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val accountRepository: AccountRepository,
) : BaseViewModel(), CreatedListInteractionListener, SaveListInteractionListener {


    private val _createdListUIState = MutableStateFlow(MyListUIState())
    val createdListUIState = _createdListUIState.asStateFlow()

    val listName = MutableLiveData("")

    private val _isCreateButtonClicked = MutableLiveData<Event<Boolean>>()
    val isButtonClicked = _isCreateButtonClicked.toLiveData()

    private val _onCLickAddEvent = MutableLiveData<Event<Boolean>>()
    val onClickAddEvent = _onCLickAddEvent.toLiveData()

//    private val _item = MutableLiveData<Event<CreatedList>>()
//    val item: LiveData<Event<CreatedList>>
//        get() = _item

    private val _newAdd = MutableLiveData(false)
    var newAdd: LiveData<Boolean> = _newAdd

    private val chosenList = MutableLiveData<CreatedList>()

    private val _message = MutableLiveData<String>()
    var message: LiveData<String> = _message

    override fun getData() {
        _createdListUIState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val sessionId = accountRepository.getSessionId()
                sessionId?.let {
                    val response = movieRepository.getAllLists(0, it).toMutableList()
                    val list = response.map { it.toUIState() }

                    _createdListUIState.update { it.copy(isLoading = false, createdList = list) }

                } ?: _createdListUIState.update { it.copy(isLoading = false, error = "NoLogin") }
            } catch (t: Throwable) {
                _createdListUIState.update {
                    it.copy(
                        isLoading = false,
                        error = t.message.toString()
                    )
                }
            }
        }
    }

    fun checkIfLogin() {
        getData()
    }

    fun onCreateList() {
        _isCreateButtonClicked.postEvent(true)
    }

    fun onClickAddList() {
        wrapWithState({
            val sessionId = accountRepository.getSessionId()
            sessionId?.let {
                val item = movieRepository.createList(it.toString(), listName.value.toString())
                if (item.success == true)
                    addList(CreatedList(item.listId ?: 0, 0, listName.value.toString()))
                listName.postValue(null)
            }
        })
        _onCLickAddEvent.postEvent(true)
    }

    private fun addList(createdLists: CreatedList) {
//        val oldList = _createdList.value?.toData()?.toMutableList()
//        oldList?.add(0, createdLists)
//        _createdList.postValue(UIState.Success(oldList))
    }

    override fun onListClick(itemID: Int) {
//        _item.postValue(Event(item))
    }

    override fun onClickSaveList(list: CreatedList) {
        chosenList.postValue(list)
        _newAdd.postValue(true)
    }

    fun checkMovie(movieId: Int) {
        wrapWithState({
            val result = movieRepository.getListDetails(chosenList.value?.id ?: 0)
            if (result.checkIfExist(movieId)) {
                _message.postValue("Fail: this movie is already on the list")
                _newAdd.postValue(false)
            }
            if (!result.checkIfExist(movieId)) {
                addMovieToList(movieId)
            }
        })

    }

    private fun addMovieToList(movieId: Int) {
        wrapWithState({
            val sessionID = accountRepository.getSessionId()
            sessionID?.let {
                movieRepository.addMovieToList(
                    it,
                    chosenList.value?.id ?: 0,
                    movieId
                )
                _message.postValue("Susses: The movie has been added")
                getData()
                _newAdd.postValue(false)
            }
        }, {
            _message.postValue("error: No Internet Connection")
            _newAdd.postValue(false)
        })
    }

}