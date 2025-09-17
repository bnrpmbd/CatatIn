package com.alphacoms.catatin.data

class ToDoRepository(private val todoDao: ToDoDao) {
    // LiveData, langsung bisa di-observe di UI
    val allTodos = todoDao.getAllTodos()
    val activeTodos = todoDao.getActiveTodos()
    val completedTodos = todoDao.getCompletedTodos()

    // operasi suspend
    suspend fun insert(todo: ToDo) = todoDao.insertToDo(todo)

    suspend fun update(todo: ToDo) = todoDao.updateToDo(todo)

    suspend fun delete(todo: ToDo) = todoDao.deleteToDo(todo)

    suspend fun updateStatus(id: Long, isCompleted: Boolean) =
        todoDao.updateToDoStatus(id, isCompleted)

    suspend fun getById(id: Long) = todoDao.getToDoById(id)
}