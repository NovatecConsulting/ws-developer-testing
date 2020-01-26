package library.books.exceptions

import library.books.domain.types.BookId
import library.exceptions.NotPossibleException

class BookAlreadyReturnedException(id: BookId)
    : NotPossibleException("The book with ID: $id was already returned!")
