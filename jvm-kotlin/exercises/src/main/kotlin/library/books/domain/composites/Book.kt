package library.books.domain.composites

import library.books.domain.types.Author
import library.books.domain.types.Isbn13
import library.books.domain.types.Title

/**
 * Composition of different book attributes into one immutable instance.
 *
 * @see Isbn13
 * @see Title
 * @see Author
 */
data class Book(
    val isbn: Isbn13,
    val title: Title,
    val authors: List<Author>,
    val numberOfPages: Int?
)
