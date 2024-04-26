/*
NOTES
I have implemented a lot of unnecessary abstraction across the app in an attempt to conform to convention despite
the app itself not requiring anything this robust. I do not like all these OO abstractions / MVVM patterns and had
I had more time I would have tried to find a way to implement more functional abstractions around the database and
viewmodel interfaces.

I am using experimental Material3 components in the app as they have the best support for Date functionality.

The Render patterns are kind of verbose and I'm sure there are better ways to handle consistent styling across the app,
but I have not had time to explore this. I have also added some random UX features in order to get a better feel for the
SDK / compose framework (Snackbar etc).

Finally, I am using a NavController to handle navigation state, it seems far more robust than the conventional alternatives.
 */




package com.example.a4_1p

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.a4_1p.database.TaskRepo
import com.example.a4_1p.database.getDatabase
import com.example.a4_1p.ui.theme._41pTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskDao = getDatabase(applicationContext).taskDao()
        val taskRepo = TaskRepo(taskDao, lifecycleScope)
        val taskViewModel = TaskViewModel(taskRepo)

        setContent {
            _41pTheme {
                TaskManagerApp(taskViewModel)
            }
        }
    }
}
