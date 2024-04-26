package com.example.a4_1p.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Some notes:
 * - I am following convention here with regard to the abstraction of the Room functionality against the Dao AND
 * TaskRepo abstraction over the Dao.
 * I do not like these patterns, in fact I generally don't like OOP which is why I'm opting for Kotlin here mainly as
 * it provides the odd opportunity to escape.
 * I am using a top level function (getDatabase) as the singleton provider for the database instance instead of a companion object
 * purely because I find it less offensive.
 */

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@Volatile // unnecessary - for threading
private var INSTANCE: AppDatabase? = null

/**
 * This is a top level function that provides a singleton instance of the database.
 * Doing it this way instead of using a companion object mainly out of spite.
 */
fun getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(AppDatabase::class.java) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "task_database"
        ).build()
        INSTANCE = instance
        instance
    }
}

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String,
    var description: String,
    var dueDate: Long
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task): Int

    @Delete
    suspend fun deleteTask(task: Task): Int
}

// implementing abstraction over Dao as it would be a good pattern in a more complex app with multiple data sources etc
class TaskRepo(private val taskDao: TaskDao, private val scope: CoroutineScope) {
    // Use StateFlow to avoid async handlers in viewmodel
    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = scope,  // Use an external coroutine scope (our App scope)
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}