package library.books

import io.mockk.every
import io.mockk.mockk
import library.books.domain.BookRecord
import library.books.domain.events.BookEvent
import library.books.domain.types.BookId
import library.events.EventDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import utils.clockWithFixedTime

internal class GetAllBooksFromCollectionTests {

    val fixedTimestamp = "2017-09-23T12:34:56.789Z"
    val fixedClock = clockWithFixedTime(fixedTimestamp)

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
    fun `getting all books delegates directly to data store`() {
        val bookRecord1 = BookRecord(BookId.generate(), Books.THE_DARK_TOWER_II)
        val bookRecord2 = BookRecord(BookId.generate(), Books.THE_DARK_TOWER_III)
        every { dataStore.findAll() } returns listOf(bookRecord1, bookRecord2)

        val allBooks = cut.getAllBooks()

        assertThat(allBooks).containsExactly(bookRecord1, bookRecord2)
    }

}
