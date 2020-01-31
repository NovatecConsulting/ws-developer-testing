from library.model import *
from library.persistence import BookDataStore
from library.events import EventDispatcher

from uuid import uuid4


class BookIdGenerator:
    def __init__(self):
        self.name = "book ID generator"

    def generate(self) -> UUID:
        uuid = uuid4()
        print(f"{self.name} - generated: {uuid}")
        return uuid


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
