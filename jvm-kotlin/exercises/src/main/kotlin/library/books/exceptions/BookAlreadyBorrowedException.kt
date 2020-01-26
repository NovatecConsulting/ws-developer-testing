package library.books.exceptions

import library.books.domain.types.BookId
import library.exceptions.NotPossibleException

class BookAlreadyBorrowedException(id: BookId)
    : NotPossibleException("The book with ID: $id is already borrowed!")
