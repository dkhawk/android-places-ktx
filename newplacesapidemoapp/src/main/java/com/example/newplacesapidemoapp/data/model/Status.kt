package com.example.newplacesapidemoapp.data.model

sealed class Status {

    data object Okay: Status() {
        override fun toString(): String = "Everything is fine"
    }
    data class Error(val message: String = "Oops"): Status() {
        override fun toString(): String = "Error: $message"
    }
}
