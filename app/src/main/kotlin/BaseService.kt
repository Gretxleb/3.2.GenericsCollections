import exceptions.NotFoundException

// Используем Generics: <T> должен быть наследником Any и иметь id: Int.
abstract class BaseService<T : Any> {
    protected val items = mutableListOf<T>()
    protected var lastId = 0

    // Абстрактный метод, который должен быть переопределен для получения ID
    abstract fun getId(item: T): Int

    // Абстрактный метод для создания копии с новым ID
    abstract fun withId(item: T, newId: Int): T

    // Создание (Create)
    fun add(item: T): T {
        lastId++
        val newItem = withId(item, lastId)
        items.add(newItem)
        return newItem
    }

    // Редактирование (Update)
    open fun edit(item: T): Boolean {
        val itemId = getId(item)
        val index = items.indexOfFirst { getId(it) == itemId }

        return if (index >= 0) {
            items[index] = item // Заменяем элемент
            true
        } else {
            false
        }
    }

    // Удаление (Delete - жесткое)
    open fun delete(id: Int): Boolean {
        return items.removeIf { getId(it) == id }
    }

    // Чтение (Read - получение списка)
    open fun get(): List<T> {
        return items.toList()
    }

    // Чтение (Read - получение по ID)
    fun getById(id: Int): T {
        return items.find { getId(it) == id } ?: throw NotFoundException("Item with ID $id not found.")
    }

    fun clear() {
        items.clear()
        lastId = 0
    }
}