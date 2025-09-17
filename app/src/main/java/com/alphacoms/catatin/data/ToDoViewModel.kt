package com.alphacoms.catatin.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alphacoms.catatin.data.ToDo
import com.alphacoms.catatin.data.ToDoRepository
import kotlinx.coroutines.launch

class ToDoViewModel(private val repository: ToDoRepository) : ViewModel() {
    val allTodos: LiveData<List<ToDo>> = repository.allTodos
    val activeTodos: LiveData<List<ToDo>> = repository.activeTodos
    val completedTodos: LiveData<List<ToDo>> = repository.completedTodos

    fun insert(todo: ToDo) {
        viewModelScope.launch {
            repository.insert(todo)
        }
    }

    fun update(todo: ToDo) {
        viewModelScope.launch {
            repository.update(todo)
        }
    }

    fun delete(todo: ToDo) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun updateStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateStatus(id, isCompleted)
        }
    }
}

class ToDoViewModelFactory(private val repository: ToDoRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}