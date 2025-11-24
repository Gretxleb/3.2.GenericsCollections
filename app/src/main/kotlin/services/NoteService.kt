package services

import models.Note
import BaseService

// NoteService просто реализует Generics для Note
class NoteService : BaseService<Note>() {

    override fun getId(item: Note) = item.id

    override fun withId(item: Note, newId: Int) = item.copy(id = newId)

    // Согласно документации VK, есть метод getComments, поэтому нам нужна ссылка на CommentService
    // В реальном проекте эта связь внедрялась бы через конструктор.
    val commentService = CommentService()

    // Дополнительный метод из документации: getById
    fun getById(id: Int): Note {
        return super.getById(id)
    }

    // Дополнительный метод из документации: delete
    override fun delete(id: Int): Boolean {
        // Ловушка: Что делать, если удаляется заметка?
        // Стратегия: Удаляем заметку, а ее комментарии остаются в CommentService,
        // но они недоступны через getComments, так как заметка отсутствует.
        return super.delete(id)
    }

    // Дополнительный метод из документации: getComments
    fun getComments(noteId: Int): List<Comment> {
        // Проверяем существование заметки
        getById(noteId) // Вызовет NotFoundException, если заметка не найдена

        // Возвращаем только НЕУДАЛЕННЫЕ комментарии к этой заметке
        return commentService.getCommentsForNote(noteId)
    }
}