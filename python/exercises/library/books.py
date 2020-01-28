from dataclasses import dataclass
from uuid import uuid4
from uuid import UUID
from typing import Optional


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


class BookIdGenerator:
    def __init__(self):
        self.name = "book ID generator"

    def generate(self) -> UUID:
        uuid = uuid4()
        print(f"{self.name} - generated: {uuid}")
        return uuid


class BookDataStore:
    def __init__(self):
        self.name = "book data store"
        self.database = {}

    def create_or_update(self, book_record: BookRecord) -> BookRecord:
        print(f"{self.name} - create or update: {book_record}")
        self.database[book_record.id] = book_record
        return book_record

    def find_by_id(self, id: UUID) -> Optional[BookRecord]:
        return self.database.get(id)


class EventDispatcher:
    def __init__(self):
        self.name = "event dispatcher"

    def dispatch(self, event):
        print(f"{self.name}: {event}")


class BookCollection:
    def __init__(self, id_generator: BookIdGenerator, data_store: BookDataStore, event_dispatcher: EventDispatcher):
        self.name = "book collection"
        self.id_generator = id_generator
        self.data_store = data_store
        self.event_dispatcher = event_dispatcher

    def add_book(self, book: Book) -> BookRecord:
        uuid = self.id_generator.generate()
        book_record = BookRecord(uuid, book)
        self.event_dispatcher.dispatch(BookAddedEvent(uuid4(), book_record))
        return self.data_store.create_or_update(book_record)

    def get_book(self, uuid: UUID) -> BookRecord:
        found = self.data_store.find_by_id(uuid)
        if found is None:
            raise Exception(f"no book record with ID [{uuid}]")
        return found
