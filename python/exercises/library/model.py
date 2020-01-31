from dataclasses import dataclass
from uuid import UUID


@dataclass
class Book:
    title: str
    isbn: str


@dataclass
class BookRecord:
    id: UUID
    book: Book


@dataclass
class BookAddedEvent:
    id: UUID
    book_record: BookRecord
