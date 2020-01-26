package exercises.assertions

import io.mockk.every
import io.mockk.mockk
import library.books.BookCollection
import library.books.BookDataStore
import library.books.BookIdGenerator
import library.books.domain.BookRecord
import library.books.domain.events.BookEvent
import library.books.domain.types.BookId
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.clockWithFixedTime

internal class GetBookFromCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val id = BookId.generate()
    val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_I)

    val dataStore: BookDataStore = mockk()
    val idGenerator: BookIdGenerator = BookIdGenerator(dataStore)
    val eventDispatcher: EventDispatcher<BookEvent> = mockk()

    val cut = BookCollection(fixedClock, dataStore, idGenerator, eventDispatcher)

    @BeforeEach
    fun setupMocks() {
        every { dataStore.existsById(any()) } returns false
        every { dataStore.delete(any()) } returns Unit
        every { eventDispatcher.dispatch(any()) } returns Unit
    }

    @Test
    fun `getting a book returns it if it was found in data store`() {
        every { dataStore.findById(id) } returns bookRecord
        val gotBook = cut.getBook(id)
    }

    @Test
    fun `getting a book throws exception if it was not found in data store`() {
        every { dataStore.findById(id) } returns null
            cut.getBook(id)
    }

}
