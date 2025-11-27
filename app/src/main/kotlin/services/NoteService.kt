package services

import models.Note
import models.Comment
import BaseService
import exceptions.NotFoundException

class NoteService : BaseService<Note>() {

    override fun getId(item: Note) = item.id

    override fun withId(item: Note, newId: Int) = item.copy(id = newId)

    val commentService = CommentService()

    fun getComments(noteId: Int): List<Comment> {
        // Метод getById() вызывается здесь. Он НАСЛЕДУЕТСЯ от BaseService.
        getById(noteId) 

        // Возвращаем только НЕУДАЛЕННЫЕ комментарии
        return commentService.getCommentsForNote(noteId)
    }

    override fun clear() {
        super.clear()
        commentService.clear()
    }
}
