package library.books

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import library.books.domain.BookRecord
import library.books.domain.events.BookAdded
import library.books.domain.events.BookBorrowed
import library.books.domain.events.BookEvent
import library.books.domain.events.BookRemoved
import library.books.domain.events.BookReturned
import library.books.domain.events.BookUpdated
import library.books.domain.states.Available
import library.books.domain.states.Borrowed
import library.books.domain.types.BookId
import library.books.domain.types.Borrower
import library.books.exceptions.BookAlreadyBorrowedException
import library.books.exceptions.BookAlreadyReturnedException
import library.books.exceptions.BookNotFoundException
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.Books
import utils.classification.UnitTest
import utils.clockWithFixedTime
import java.time.OffsetDateTime

@UnitTest
internal class BookCollectionTest {

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
        every { dataStore.delete(any()) } returns Unit
        every { eventDispatcher.dispatch(any()) } returns Unit
    }

    @Nested
    inner class `adding a book` {

        @Test
        fun `generates a new book ID`() {
            with(cut.addBook(Books.THE_MARTIAN)) {
                assertThat(id).isNotNull()
            }
        }

        @Test
        fun `sets the initial state to available`() {
            with(cut.addBook(Books.THE_MARTIAN)) {
                assertThat(state).isEqualTo(Available)
            }
        }

        @Test
        fun `stores the book's data`() {
            with(cut.addBook(Books.THE_MARTIAN)) {
                assertThat(book).isEqualTo(Books.THE_MARTIAN)
            }
        }

        @Test
        fun `dispatches a BookAdded event`() {
            val eventSlot = slot<BookAdded>()
            every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit

            val bookRecord = cut.addBook(Books.THE_MARTIAN)

            with(eventSlot.captured) {
                assertThat(bookId).isEqualTo("${bookRecord.id}")
                assertThat(timestamp).isEqualTo(fixedTimestamp)
            }
        }

        @Test
        fun `does not dispatch any events in case of an exception`() {
            every { dataStore.createOrUpdate(any()) } throws RuntimeException()
            assertThrows<RuntimeException> {
                cut.addBook(Books.THE_MARTIAN)
            }
            confirmVerified(eventDispatcher)
        }

    }

    @Nested
    inner class `updating a book` {

        val id = BookId.generate()
        val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_VII)
        val updatedBookRecord = bookRecord.changeNumberOfPages(42)

        @Test
        fun `updates the record in the database`() {
            every { dataStore.findById(id) } returns bookRecord

            val updatedBook = cut.updateBook(id) { updatedBookRecord }

            assertThat(updatedBook).isEqualTo(updatedBook)
            verify { dataStore.createOrUpdate(updatedBook) }
        }

        @Test
        fun `dispatches a BookUpdated event`() {
            val eventSlot = slot<BookUpdated>()
            every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
            every { dataStore.findById(id) } returns bookRecord

            cut.updateBook(id) { updatedBookRecord }

            val event = eventSlot.captured
            assertThat(event.bookId).isEqualTo("$id")
            assertThat(event.timestamp).isEqualTo(fixedTimestamp)
        }

        @Test
        fun `throws exception if it was not found in data store`() {
            every { dataStore.findById(id) } returns null
            assertThrows<BookNotFoundException> {
                cut.updateBook(id) { updatedBookRecord }
            }
        }

        @Test
        fun `does not dispatch any events in case of an exception`() {
            every { dataStore.createOrUpdate(any()) } throws RuntimeException()
            assertThrows<RuntimeException> {
                cut.updateBook(id) { updatedBookRecord }
            }
            confirmVerified(eventDispatcher)
        }

    }

    @Nested
    inner class `getting a book` {

        val id = BookId.generate()
        val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_I)

        @Test
        fun `returns it if it was found in data store`() {
            every { dataStore.findById(id) } returns bookRecord
            val gotBook = cut.getBook(id)
            assertThat(gotBook).isEqualTo(bookRecord)
        }

        @Test
        fun `throws exception if it was not found in data store`() {
            every { dataStore.findById(id) } returns null
            assertThrows<BookNotFoundException> {
                cut.getBook(id)
            }
        }

    }

    @Nested
    inner class `getting all books` {

        @Test
        fun `delegates directly to data store`() {
            val bookRecord1 = BookRecord(BookId.generate(), Books.THE_DARK_TOWER_II)
            val bookRecord2 = BookRecord(BookId.generate(), Books.THE_DARK_TOWER_III)
            every { dataStore.findAll() } returns listOf(bookRecord1, bookRecord2)

            val allBooks = cut.getAllBooks()

            assertThat(allBooks).containsExactly(bookRecord1, bookRecord2)
        }

    }

    @Nested
    inner class `removing a book` {

        val id = BookId.generate()
        val bookRecord = BookRecord(id, Books.THE_DARK_TOWER_IV)

        @Test
        fun `deletes it from the data store if found`() {
            every { dataStore.findById(id) } returns bookRecord
            cut.removeBook(id)
            verify { dataStore.delete(bookRecord) }
        }

        @Test
        fun `dispatches a BookRemoved event`() {
            val eventSlot = slot<BookRemoved>()
            every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
            every { dataStore.findById(id) } returns bookRecord

            cut.removeBook(id)

            val event = eventSlot.captured
            assertThat(event.bookId).isEqualTo("$id")
            assertThat(event.timestamp).isEqualTo(fixedTimestamp)
        }

        @Test
        fun `throws exception if it was not found in data store`() {
            every { dataStore.findById(id) } returns null
            assertThrows<BookNotFoundException> {
                cut.removeBook(id)
            }
        }

        @Test
        fun `does not dispatch any events in case of an exception`() {
            every { dataStore.findById(id) } throws RuntimeException()
            assertThrows<RuntimeException> {
                cut.removeBook(id)
            }
            confirmVerified(eventDispatcher)
        }

    }

    @Nested
    inner class `borrowing a book` {

        val id = BookId.generate()
        val availableBookRecord = BookRecord(id, Books.THE_DARK_TOWER_V)
        val borrowedBookRecord = availableBookRecord.borrow(Borrower("Someone"), OffsetDateTime.now())

        @Test
        fun `changes its state and updates it in the data store`() {
            every { dataStore.findById(id) } returns availableBookRecord

            val borrowedBook = cut.borrowBook(id, Borrower("Someone"))

            assertThat(borrowedBook.state).isInstanceOf(Borrowed::class.java)
            assertThat(borrowedBook).isEqualTo(borrowedBook)
        }

        @Test
        fun `dispatches a BookBorrowed event`() {
            val eventSlot = slot<BookBorrowed>()
            every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
            every { dataStore.findById(id) } returns availableBookRecord

            cut.borrowBook(id, Borrower("Someone"))

            val event = eventSlot.captured
            assertThat(event.bookId).isEqualTo("$id")
            assertThat(event.timestamp).isEqualTo(fixedTimestamp)
        }

        @Test
        fun `throws exception if it was not found in data store`() {
            every { dataStore.findById(id) } returns null
            assertThrows<BookNotFoundException> {
                cut.borrowBook(id, Borrower("Someone"))
            }
        }

        @Test
        fun `throws exception if it is already 'borrowed'`() {
            every { dataStore.findById(id) } returns borrowedBookRecord
            assertThrows<BookAlreadyBorrowedException> {
                cut.borrowBook(id, Borrower("Someone Else"))
            }
        }

        @Test
        fun `does not dispatch any events in case of an exception`() {
            every { dataStore.findById(id) } throws RuntimeException()
            assertThrows<RuntimeException> {
                cut.borrowBook(id, Borrower("Someone Else"))
            }
            confirmVerified(eventDispatcher)
        }

    }

    @Nested
    inner class `returning a book` {

        val id = BookId.generate()
        val availableBookRecord = BookRecord(id, Books.THE_DARK_TOWER_VI)
        val borrowedBookRecord = availableBookRecord.borrow(Borrower("Someone"), OffsetDateTime.now())

        @Test
        fun `changes its state and updates it in the data store`() {
            every { dataStore.findById(id) } returns borrowedBookRecord

            val result = cut.returnBook(id)

            assertThat(result.state).isEqualTo(Available)
            assertThat(result).isEqualTo(availableBookRecord)
        }

        @Test
        fun `dispatches a BookReturned event`() {
            val eventSlot = slot<BookReturned>()
            every { eventDispatcher.dispatch(capture(eventSlot)) } returns Unit
            every { dataStore.findById(id) } returns borrowedBookRecord

            cut.returnBook(id)

            val event = eventSlot.captured
            assertThat(event.bookId).isEqualTo("$id")
            assertThat(event.timestamp).isEqualTo(fixedTimestamp)
        }

        @Test
        fun `throws exception if it was not found in data store`() {
            every { dataStore.findById(id) } returns null
            assertThrows<BookNotFoundException> {
                cut.returnBook(id)
            }
        }

        @Test
        fun `throws exception if it is already 'returned'`() {
            every { dataStore.findById(id) } returns availableBookRecord
            assertThrows<BookAlreadyReturnedException> {
                cut.returnBook(id)
            }
        }

        @Test
        fun `does not dispatch any events in case of an exception`() {
            every { dataStore.findById(id) } throws RuntimeException()
            assertThrows<RuntimeException> {
                cut.returnBook(id)
            }
            confirmVerified(eventDispatcher)
        }

    }

}
