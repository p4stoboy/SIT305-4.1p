package com.example.a4_1p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4_1p.database.Task
import com.example.a4_1p.database.TaskRepo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepo: TaskRepo) : ViewModel() {
    val tasks: StateFlow<List<Task>> = taskRepo.allTasks

    fun getTask(taskId: Int): Task? {
        return tasks.value.find { it.id == taskId }
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepo.insert(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepo.update(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepo.delete(task)
    }
}