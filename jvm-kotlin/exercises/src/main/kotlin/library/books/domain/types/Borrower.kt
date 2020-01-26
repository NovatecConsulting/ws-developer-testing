package library.books.domain.types

import library.books.domain.BookRecord
import library.books.domain.states.Borrowed

/** Person who [Borrowed] a [BookRecord]. */
data class Borrower(
    private val value: String
) {
    override fun toString(): String = value
}
