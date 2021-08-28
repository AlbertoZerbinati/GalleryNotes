package com.esp.gallerynotes.database
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTask(id:Int) : LiveData<Task>

    @Insert()
    fun insertTask(task:Task)

    @Update()
    fun updateTask(task:Task)

    @Delete
    fun deleteTask(task:Task)
}