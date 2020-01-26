package library.books

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.domain.BookRecord
import library.books.domain.events.BookEvent
import library.books.domain.events.BookReturned
import library.books.domain.states.Available
import library.books.domain.types.BookId
import library.books.domain.types.Borrower
import library.books.exceptions.BookAlreadyReturnedException
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.clockWithFixedTime
import java.time.OffsetDateTime

internal class ReturnBookToCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val id = BookId.generate()
    val availableBookRecord = BookRecord(id, Books.THE_DARK_TOWER_VI)
    val borrowedBookRecord = availableBookRecord.borrow(Borrower("Someone"), OffsetDateTime.now())

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
    fun `returning a book changes its state and updates it in the data store`() {
        every { dataStore.findById(id) } returns borrowedBookRecord

        val result = cut.returnBook(id)

        assertThat(result.state).isEqualTo(Available)
        assertThat(result).isEqualTo(availableBookRecord)
    }

    @Test
    fun `returning a book dispatches a BookReturned event`() {
        val eventSlot = slot<BookReturned>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
        every { dataStore.findById(id) } returns borrowedBookRecord

        cut.returnBook(id)

        val event = eventSlot.captured
        assertThat(event.bookId).isEqualTo("$id")
        assertThat(event.timestamp).isEqualTo(fixedTimestamp)
    }

    @Test
    fun `returning a book throws exception if it was not found in data store`() {
        every { dataStore.findById(id) } returns null
        assertThrows<BookNotFoundException> {
            cut.returnBook(id)
        }
    }

    @Test
    fun `returning a book throws exception if it is already 'returned'`() {
        every { dataStore.findById(id) } returns availableBookRecord
        assertThrows<BookAlreadyReturnedException> {
            cut.returnBook(id)
        }
    }

    @Test
    fun `returning a book does not dispatch any events in case of an exception`() {
        every { dataStore.findById(id) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.returnBook(id)
        }
        verify { eventDispatcher wasNot called }
    }

}
