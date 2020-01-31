from unittest import TestCase, main
from unittest.mock import Mock

from library.books import *
from tests.testdata import *


class TestAddBookToCollection(TestCase):
    cut = None

    def setUp(self):
        data_store = BookDataStore()
        id_generator = BookIdGenerator(data_store)
        event_dispatcher = Mock()
        self.cut = BookCollection(id_generator, data_store, event_dispatcher)

    def test_adding_a_book_generates_a_new_book_id(self):
        book_record = self.cut.add_book(book_the_martian)

    def test_adding_a_book_stores_the_books_data(self):
        book_record = self.cut.add_book(book_the_martian)

    def test_adding_a_book_sets_its_initial_state_to_available(self):
        book_record = self.cut.add_book(book_the_martian)

    def test_adding_a_book_dispatches_a_BookAdded_event(self):
        book_record = self.cut.add_book(book_the_martian)

    def test_adding_a_book_does_not_dispatch_any_events_in_case_of_an_error(self):
        self.cut.data_store = Mock()
        self.cut.data_store.create_or_update.side_effect = Exception
        self.cut.add_book(book_the_martian)


if __name__ == '__main__':
    main()
