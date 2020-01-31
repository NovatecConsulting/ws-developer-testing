from dataclasses import dataclass
from datetime import datetime
from uuid import UUID


class BookState:
    pass


@dataclass
class Available(BookState):
    pass


@dataclass
class Borrowed(BookState):
    by: str
    on: datetime


@dataclass
class Book:
    title: str
    isbn: str


@dataclass
class BookRecord:
    id: UUID
    book: Book
    state: BookState


@dataclass
class BookAdded:
    id: UUID
    book_record: BookRecord


@dataclass
class BookBorrowed:
    id: UUID
    book_record: BookRecord
    borrower: str
