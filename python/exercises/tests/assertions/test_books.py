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


class TestBorrowBookFromCollection(TestCase):
    cut = None

    def setUp(self):
        data_store = BookDataStore()
        id_generator = BookIdGenerator(data_store)
        event_dispatcher = Mock()
        self.cut = BookCollection(id_generator, data_store, event_dispatcher)

    def test_borrowing_a_book_changes_its_state_and_updates_it_in_the_data_store(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Available())
        self.cut.data_store = Mock()
        self.cut.data_store.find_by_id.return_value = book_record
        self.cut.data_store.create_or_update.return_value = book_record

        self.cut.borrow_book(book_id, "Bob")

    def test_borrowing_a_book_dispatches_a_BookBorrowed_event(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Available())
        self.cut.data_store = Mock()
        self.cut.data_store.find_by_id.return_value = book_record
        self.cut.data_store.create_or_update.return_value = book_record

        self.cut.borrow_book(book_id, "Bob")
        # dispatched_event = self.cut.event_dispatcher.dispatch.call_args[0][0]

    def test_borrowing_a_book_throws_exception_if_it_was_not_found_in_data_store(self):
        self.cut.data_store = Mock()
        self.cut.data_store.find_by_id.side_effect = NotFoundException
        self.cut.borrow_book(uuid4(), "Bob")

    def test_borrowing_a_book_throws_exception_if_it_is_already_borrowed(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Borrowed("Amy", datetime.now()))
        self.cut.data_store = Mock()
        self.cut.data_store.find_by_id.return_value = book_record
        self.cut.borrow_book(book_id, "Bob")

    def test_borrowing_a_book_does_not_dispatch_any_events_in_case_of_an_exception(self):
        self.cut.data_store.find_by_id.side_effect = Exception
        self.cut.borrow_book(uuid4(), "Bob")


if __name__ == '__main__':
    main()
