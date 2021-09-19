package com.esp.gallerynotes.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esp.gallerynotes.database.Task

class SharedTaskViewModel: ViewModel() {
    val selectedTask : MutableLiveData<Task?> = MutableLiveData()
}