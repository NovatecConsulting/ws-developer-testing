package library.books.exceptions

import library.books.domain.types.BookId
import library.exceptions.NotFoundException

class BookNotFoundException(id: BookId)
    : NotFoundException("The book with ID: $id does not exist!")
