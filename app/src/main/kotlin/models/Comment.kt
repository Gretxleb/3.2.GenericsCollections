package models

data class Comment(
    val id: Int = 0,
    val noteId: Int, // ID заметки, к которой относится комментарий
    val text: String,
    val date: Long = System.currentTimeMillis() / 1000,
    val isDeleted: Boolean = false // Флаг мягкого удаления
)