package com.karrar.movieapp.domain

sealed class FormFieldState {

    object Valid : FormFieldState()
    data class InValid(val message: String) : FormFieldState()

    fun errorMessage() = if (this is InValid) message else null

    fun isValidField(): Boolean {
        return this is Valid
    }
}