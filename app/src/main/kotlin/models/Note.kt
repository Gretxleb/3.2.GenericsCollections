package models

data class Note(
    val id: Int = 0,
    val title: String,
    val text: String,
    val date: Long = System.currentTimeMillis() / 1000
)
