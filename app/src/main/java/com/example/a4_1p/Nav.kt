package com.example.a4_1p

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskManagerApp(taskViewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "task_list") {
        composable("task_list") {
            TaskListScreen(navController, taskViewModel)
        }
        composable(
            route = "add_edit_task?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            val actualTaskId = if (taskId == -1) null else taskId
            AddEditTaskScreen(navController, taskViewModel, actualTaskId)
        }
    }
}

