package library.books

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.domain.events.BookAdded
import library.books.domain.events.BookEvent
import library.books.domain.states.Available
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(id).isNotNull()
        }
    }

    @Test
    fun `adding a book sets the initial state to available`() {
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(state).isEqualTo(Available)
        }
    }

    @Test
    fun `adding a book stores the book's data`() {
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(book).isEqualTo(Books.THE_MARTIAN)
        }
    }

    @Test
    fun `adding a book dispatches a BookAdded event`() {
        val eventSlot = slot<BookAdded>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit

        val bookRecord = cut.addBook(Books.THE_MARTIAN)

        with(eventSlot.captured) {
            assertThat(bookId).isEqualTo("${bookRecord.id}")
            assertThat(timestamp).isEqualTo(fixedTimestamp)
        }
    }

    @Test
    fun `adding a book does not dispatch any events in case of an exception`() {
        every { dataStore.createOrUpdate(any()) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.addBook(Books.THE_MARTIAN)
        }
        verify { eventDispatcher wasNot called }
    }

}
