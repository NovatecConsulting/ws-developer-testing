from library.model import *

from uuid import UUID
from typing import Optional


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
