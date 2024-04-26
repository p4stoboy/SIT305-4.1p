package com.example.a4_1p

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a4_1p.database.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskListScreen(nc: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.tasks
        .map { tasks -> tasks.sortedBy { task -> task.dueDate } }
        .collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nc.navigate("add_edit_task") },
                shape = RectangleShape,
                containerColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {  // Use Column to stack elements vertically
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "My Tasks", fontWeight = FontWeight.Bold)
            }

            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f).padding(bottom = 78.dp)) {
                if (tasks.isEmpty()) {
                    item {
                        Text("No tasks yet!", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(items = tasks, key = { task -> task.id }) { task ->
                        TaskItem(nc, viewModel, task = task)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun stringDate(timestamp: Long): String {
    val date = LocalDate.ofEpochDay(timestamp)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    return date.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(nc: NavController, viewModel: TaskViewModel, task: Task) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .border(1.dp, Color.Black)
            .padding(8.dp)
    ) {
        Text(text = task.title, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text = task.description, modifier = Modifier.fillMaxWidth().padding(2.dp).padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
            Text(text = "due: " + stringDate(task.dueDate), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(
                onClick = { nc.navigate("add_edit_task?taskId=${task.id}") },
                modifier = Modifier.defaultMinSize(minWidth = 32.dp).height(height = 32.dp).padding(end = 16.dp),
                shape = RectangleShape
            ) {
                Text(text = "Edit")
            }
            Button(
                onClick = { try { viewModel.deleteTask(task)} catch (e: Exception) { println("Error: $e") } },
                modifier = Modifier.defaultMinSize(minWidth = 32.dp).height(height = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape
            ) {
                Text("Delete")
            }
        }
    }
}

/*
using the experimental Material3 API because there is weirdly no other robust built-in date picker in Compose
*/
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(nc: NavController, viewModel: TaskViewModel, id: Int? = null) {
    // check for edit (as opposed to new Task)
    val task = id?.let { viewModel.getTask(it) }

    val title = remember { mutableStateOf(task?.title ?: "") }
    val description = remember { mutableStateOf(task?.description ?: "") }
    val dueDate = remember { mutableStateOf(task?.dueDate?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()) }

    // input error states
    val titleError = remember { mutableStateOf(false) }
    val descriptionError = remember { mutableStateOf(false) }
    val dueDateError = remember { mutableStateOf(false) }

    // for confirmation message
    val showSnackbar = remember { mutableStateOf(false) }
    val snackbarMessage = remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    )
    val datePickerVisible = remember { mutableStateOf(false) }

    if (datePickerVisible.value) {
        DatePickerDialog(
            onDismissRequest = { datePickerVisible.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDueDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            println("Updating due date to: $newDueDate")  // Debug log
                            dueDate.value = newDueDate
                        }
                        datePickerVisible.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerVisible.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError.value
        )
        if (titleError.value) {
            Text(
                text = "Please enter a title",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            isError = descriptionError.value
        )
        if (descriptionError.value) {
            Text(
                text = "Please enter a description",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        OutlinedTextField(
            value = dueDate.value.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
            onValueChange = { },
            label = { Text("Due Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerVisible.value = true },
            enabled = false
        )
        if (dueDateError.value) {
            Text(
                text = "Please select a future date",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Row {
            Button(
                onClick = {
                    titleError.value = title.value.isBlank()
                    descriptionError.value = description.value.isBlank()
                    dueDateError.value = dueDate.value.isBefore(LocalDate.now())
                    println("Due Date: ${dueDate.value}")  // Log the LocalDate value
                    if (!titleError.value && !descriptionError.value && !dueDateError.value) {
                        try {
                            // check for edit
                            if (id != null) {
                                task?.let {
                                    viewModel.updateTask(
                                        it.copy(
                                            title = title.value,
                                            description = description.value,
                                            dueDate = dueDate.value.toEpochDay()
                                        )
                                    )
                                }
                            } else {
                                viewModel.insertTask(
                                    Task(
                                        title = title.value,
                                        description = description.value,
                                        dueDate = dueDate.value.toEpochDay()
                                    )
                                )
                            }
                            snackbarMessage.value = if (id != null) "Task updated successfully" else "Task saved successfully"
                        } catch (e: Exception) {
                            snackbarMessage.value = "Error: Something went wrong while saving task."
                            println("Error: $e")  // Log the exception
                        }
                        showSnackbar.value = true
                    }
                },
                shape = RectangleShape
            ) {
                Text("Save")
            }
            Button(
                onClick = { nc.navigateUp() },
                shape = RectangleShape,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("Cancel")
            }

            if (showSnackbar.value) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(text = snackbarMessage.value)
                }
                LaunchedEffect(Unit) {
                    delay(1500L)
                    showSnackbar.value = false
                    nc.navigateUp()
                }
            }
        }
    }
}
