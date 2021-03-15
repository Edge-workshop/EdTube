package dev.kingkongcode.edtube.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class HomePageViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePageViewModel::class.java)) {
            return HomePageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}