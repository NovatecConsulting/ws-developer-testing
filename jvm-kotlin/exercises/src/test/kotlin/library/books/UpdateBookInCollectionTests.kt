package library.books

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.domain.BookRecord
import library.books.domain.events.BookEvent
import library.books.domain.events.BookUpdated
import library.books.domain.types.BookId
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.clockWithFixedTime

internal class UpdateBookInCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val id = BookId.generate()
    val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_VII)
    val updatedBookRecord = bookRecord.changeNumberOfPages(42)

    val dataStore: BookDataStore = mockk {
        every { createOrUpdate(any()) } answers { firstArg() }
    }
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
    fun `updating a book updates the record in the database`() {
        every { dataStore.findById(id) } returns bookRecord

        val updatedBook = cut.updateBook(id) { updatedBookRecord }

        assertThat(updatedBook).isEqualTo(updatedBook)
        verify { dataStore.createOrUpdate(updatedBook) }
    }

    @Test
    fun `updating a book dispatches a BookUpdated event`() {
        val eventSlot = slot<BookUpdated>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
        every { dataStore.findById(id) } returns bookRecord

        cut.updateBook(id) { updatedBookRecord }

        val event = eventSlot.captured
        assertThat(event.bookId).isEqualTo("$id")
        assertThat(event.timestamp).isEqualTo(fixedTimestamp)
    }

    @Test
    fun `updating a book throws exception if it was not found in data store`() {
        every { dataStore.findById(id) } returns null
        assertThrows<BookNotFoundException> {
            cut.updateBook(id) { updatedBookRecord }
        }
    }

    @Test
    fun `updating a book does not dispatch any events in case of an exception`() {
        every { dataStore.createOrUpdate(any()) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.updateBook(id) { updatedBookRecord }
        }
        verify { eventDispatcher wasNot called }
    }

}
