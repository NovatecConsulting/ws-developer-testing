package exercises.naming

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.BookCollection
import library.books.BookDataStore
import library.books.BookIdGenerator
import library.books.domain.BookRecord
import library.books.domain.events.BookEvent
import library.books.domain.events.BookRemoved
import library.books.domain.types.BookId
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.clockWithFixedTime

internal class RemoveBookFromCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val id = BookId.generate()
    val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_IV)

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
    fun `good case 1`() {
        every { dataStore.findById(id) } returns bookRecord
        cut.removeBook(id)
        verify { dataStore.delete(bookRecord) }
    }

    @Test
    fun `good case 2`() {
        val eventSlot = slot<BookRemoved>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
        every { dataStore.findById(id) } returns bookRecord

        cut.removeBook(id)

        val event = eventSlot.captured
        assertThat(event.bookId).isEqualTo("$id")
        assertThat(event.timestamp).isEqualTo(fixedTimestamp)
    }

    @Test
    fun `bad case 1`() {
        every { dataStore.findById(id) } returns null
        assertThrows<BookNotFoundException> {
            cut.removeBook(id)
        }
    }

    @Test
    fun `bad case 2`() {
        every { dataStore.findById(id) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.removeBook(id)
        }
        verify { eventDispatcher wasNot called }
    }

}
