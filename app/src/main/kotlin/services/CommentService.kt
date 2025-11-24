package services

import models.Comment
import BaseService
import exceptions.AlreadyDeletedException
import exceptions.NotFoundException

class CommentService : BaseService<Comment>() {

    override fun getId(item: Comment) = item.id

    override fun withId(item: Comment, newId: Int) = item.copy(id = newId)

    // Добавление комментария (createComment)
    fun createComment(noteId: Int, comment: Comment): Comment {
        // ВАЖНО: В реальном проекте здесь должна быть проверка на существование noteId
        // Мы ее выносим в NoteService.createComment, но для простоты этого класса
        // мы предполагаем, что noteId корректен.
        return add(comment.copy(noteId = noteId))
    }

    // Редактирование комментария (editComment)
    override fun edit(item: Comment): Boolean {
        val itemId = getId(item)
        val index = items.indexOfFirst { getId(it) == itemId }

        if (index < 0) return false // Комментарий не найден

        val existingComment = items[index]

        // Ловушка: Нельзя редактировать удаленный комментарий
        if (existingComment.isDeleted) {
            throw AlreadyDeletedException("Comment with ID $itemId is already deleted and cannot be edited.")
        }

        // Обновляем только те поля, которые разрешены (ID, text, isDeleted - сохраняем)
        items[index] = existingComment.copy(
            text = item.text,
            isDeleted = false // Если редактируем, то он точно не удален
        )
        return true
    }

    // Удаление комментария (deleteComment - мягкое удаление)
    override fun delete(id: Int): Boolean {
        val index = items.indexOfFirst { getId(it) == id }
        if (index < 0) return false // Комментарий не найден

        val existingComment = items[index]

        // Ловушка: Что делать, если пользователь пытается удалить уже удаленный?
        if (existingComment.isDeleted) {
            throw AlreadyDeletedException("Comment with ID $id is already deleted.")
        }

        // Выполняем мягкое удаление
        items[index] = existingComment.copy(isDeleted = true)
        return true
    }

    // Восстановление удаленного комментария (restoreComment)
    fun restoreComment(id: Int): Boolean {
        val index = items.indexOfFirst { getId(it) == id }
        if (index < 0) return false // Комментарий не найден

        val existingComment = items[index]

        // Ловушка: Что делать, если пользователь пытается восстановить неудаленный комментарий?
        if (!existingComment.isDeleted) {
            // Стратегия: ничего не делаем, просто возвращаем true
            return true
        }

        // Восстанавливаем
        items[index] = existingComment.copy(isDeleted = false)
        return true
    }

    // Вспомогательный метод для NoteService
    fun getCommentsForNote(noteId: Int): List<Comment> {
        return items.filter { it.noteId == noteId && !it.isDeleted }
    }
}