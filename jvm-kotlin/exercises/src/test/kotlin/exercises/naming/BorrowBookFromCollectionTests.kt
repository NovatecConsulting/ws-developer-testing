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
import library.books.domain.events.BookBorrowed
import library.books.domain.events.BookEvent
import library.books.domain.states.Borrowed
import library.books.domain.types.BookId
import library.books.domain.types.Borrower
import library.books.exceptions.BookAlreadyBorrowedException
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.clockWithFixedTime
import java.time.OffsetDateTime

internal class BorrowBookFromCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

    val id = BookId.generate()
    val availableBookRecord = BookRecord(id, Books.THE_DARK_TOWER_V)
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
    fun `updates data`() {
        every { dataStore.findById(id) } returns availableBookRecord

        val borrowedBook = cut.borrowBook(id, Borrower("Someone"))

        assertThat(borrowedBook.state).isInstanceOf(Borrowed::class.java)
        assertThat(borrowedBook).isEqualTo(borrowedBook)
    }

    @Test
    fun `events`() {
        val eventSlot = slot<BookBorrowed>()
        every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
        every { dataStore.findById(id) } returns availableBookRecord

        cut.borrowBook(id, Borrower("Someone"))

        val event = eventSlot.captured
        assertThat(event.bookId).isEqualTo("$id")
        assertThat(event.timestamp).isEqualTo(fixedTimestamp)
    }

    @Test
    fun `not found`() {
        every { dataStore.findById(id) } returns null
        assertThrows<BookNotFoundException> {
            cut.borrowBook(id, Borrower("Someone"))
        }
    }

    @Test
    fun `already borrowed`() {
        every { dataStore.findById(id) } returns borrowedBookRecord
        assertThrows<BookAlreadyBorrowedException> {
            cut.borrowBook(id, Borrower("Someone Else"))
        }
    }

    @Test
    fun `events when exception`() {
        every { dataStore.findById(id) } throws RuntimeException()
        assertThrows<RuntimeException> {
            cut.borrowBook(id, Borrower("Someone Else"))
        }
        verify { eventDispatcher wasNot called }
    }

}
