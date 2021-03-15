package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class SearchVideoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchVideoViewModel::class.java)) {
            return SearchVideoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}