package exercises.naming

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.BookCollection
import library.books.BookDataStore
import library.books.BookIdGenerator
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
    fun test1() {
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(id).isNotNull()
        }
    }

    @Test
    fun test2() {
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(state).isEqualTo(Available)
        }
    }

    @Test
    fun test3() {
        with(cut.addBook(Books.THE_MARTIAN)) {
            assertThat(book).isEqualTo(Books.THE_MARTIAN)
        }
    }

    @Test
    fun test4() {
        val eventSlot = slot<BookAdded>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit

        val bookRecord = cut.addBook(Books.THE_MARTIAN)

        with(eventSlot.captured) {
            assertThat(bookId).isEqualTo("${bookRecord.id}")
            assertThat(timestamp).isEqualTo(fixedTimestamp)
        }
    }

    @Test
    fun test5() {
        every { dataStore.createOrUpdate(any()) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.addBook(Books.THE_MARTIAN)
        }
        verify { eventDispatcher wasNot called }
    }

}
