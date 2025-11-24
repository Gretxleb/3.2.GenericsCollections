import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import services.NoteService
import exceptions.NotFoundException
import exceptions.AlreadyDeletedException
import models.Note
import models.Comment

class NoteServiceTest {
    private lateinit var service: NoteService

    @BeforeEach
    fun setup() {
        service = NoteService()
        service.clear()
        service.commentService.clear()
    }

    // ---------------------- NoteService CRUD ----------------------

    @Test
    fun noteService_add_createsNewNoteWithId() {
        val newNote = Note(title = "Title", text = "Text")
        val addedNote = service.add(newNote)
        assertTrue(addedNote.id > 0)
    }

    @Test
    fun noteService_getById_shouldFindExistingNote() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val foundNote = service.getById(note.id)
        assertEquals(note, foundNote)
    }

    @Test
    fun noteService_getById_shouldThrowExceptionForNonExistingNote() {
        assertThrows<NotFoundException> {
            service.getById(999)
        }
    }

    // ---------------------- CommentService (CRUD und Soft Delete) ----------------------

    @Test
    fun createComment_shouldAddCommentToNote() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val newComment = Comment(noteId = note.id, text = "New Comment")
        val addedComment = service.commentService.createComment(note.id, newComment)
        assertTrue(addedComment.id > 0)
    }

    @Test
    fun getComments_shouldReturnOnlyNotDeletedCommentsForNote() {
        val note1 = service.add(Note(title = "N1", text = "T1"))
        val note2 = service.add(Note(title = "N2", text = "T2"))

        service.commentService.createComment(note1.id, Comment(noteId = note1.id, text = "C1"))
        val deletedComment = service.commentService.createComment(note1.id, Comment(noteId = note1.id, text = "C2 to delete"))
        service.commentService.createComment(note2.id, Comment(noteId = note2.id, text = "C3 on N2"))

        service.commentService.delete(deletedComment.id) // Мягкое удаление C2

        val comments = service.getComments(note1.id)

        assertEquals(1, comments.size)
        assertEquals("C1", comments.first().text)
    }

    @Test
    fun deleteComment_shouldSetIsDeletedTrue() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val comment = service.commentService.createComment(note.id, Comment(noteId = note.id, text = "Text"))

        service.commentService.delete(comment.id)
        val deletedComment = service.commentService.getById(comment.id)

        assertTrue(deletedComment.isDeleted)
    }

    @Test
    fun deleteComment_shouldThrowAlreadyDeletedExceptionIfAlreadyDeleted() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val comment = service.commentService.createComment(note.id, Comment(noteId = note.id, text = "Text"))
        service.commentService.delete(comment.id)

        assertThrows<AlreadyDeletedException> {
            service.commentService.delete(comment.id)
        }
    }

    @Test
    fun editComment_shouldThrowAlreadyDeletedExceptionIfDeleted() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val comment = service.commentService.createComment(note.id, Comment(noteId = note.id, text = "Old Text"))
        service.commentService.delete(comment.id)

        assertThrows<AlreadyDeletedException> {
            service.commentService.edit(comment.copy(text = "New Text"))
        }
    }

    @Test
    fun restoreComment_shouldRestoreDeletedComment() {
        val note = service.add(Note(title = "Test", text = "Test"))
        val comment = service.commentService.createComment(note.id, Comment(noteId = note.id, text = "Text"))
        service.commentService.delete(comment.id)

        val result = service.commentService.restoreComment(comment.id)
        val restoredComment = service.commentService.getById(comment.id)

        assertTrue(result)
        assertFalse(restoredComment.isDeleted)
    }

    @Test
    fun getComments_shouldThrowNotFoundExceptionIfNoteIsDeleted() {
        val note = service.add(Note(title = "Test", text = "Test"))
        service.commentService.createComment(note.id, Comment(noteId = note.id, text = "Comment"))

        service.delete(note.id) // Удаляем заметку

        assertThrows<NotFoundException> {
            service.getComments(note.id) // Пытаемся получить комментарии
        }
    }
}