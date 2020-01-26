package exercises.assertions

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import library.books.BookCollection
import library.books.BookDataStore
import library.books.BookIdGenerator
import library.books.domain.events.BookAdded
import library.books.domain.events.BookEvent
import library.events.EventDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import utils.clockWithFixedTime

internal class AddBookToCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val dataStore: BookDataStore = mockk {
        every { createOrUpdate(any()) } answers { firstArg() }
    }
    val idGenerator: BookIdGenerator = BookIdGenerator(dataStore)
    val eventDispatcher: EventDispatcher<BookEvent> = mockk()

    val cut = BookCollection(fixedClock, dataStore, idGenerator, eventDispatcher)

    @BeforeEach
    fun setupMocks() {
        every { dataStore.existsById(any()) } returns false
        every { eventDispatcher.dispatch(any()) } returns Unit
    }

    @Test
    fun `adding a book generates a new book ID`() {
        val bookRecord = cut.addBook(Books.THE_MARTIAN)
    }

    @Test
    fun `adding a book sets the initial state to available`() {
        val bookRecord = cut.addBook(Books.THE_MARTIAN)
    }

    @Test
    fun `adding a book stores the book's data`() {
        val bookRecord = cut.addBook(Books.THE_MARTIAN)
    }

    @Test
    fun `adding a book dispatches a BookAdded event`() {
        val eventSlot = slot<BookAdded>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit

        val bookRecord = cut.addBook(Books.THE_MARTIAN)
    }

    @Test
    fun `adding a book does not dispatch any events in case of an exception`() {
        every { dataStore.createOrUpdate(any()) } throws RuntimeException()
        cut.addBook(Books.THE_MARTIAN)
    }

}
