from unittest import TestCase, main
from unittest.mock import Mock
from datetime import datetime, timedelta

from library.books import *
from tests.testdata import *


class TestAddBookToCollection(TestCase):
    cut = None

    def setUp(self):
        data_store = BookDataStore()
        id_generator = BookIdGenerator(data_store)
        event_dispatcher = Mock()
        self.cut = BookCollection(id_generator, data_store, event_dispatcher)

    def test_1(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertIsInstance(book_record.id, UUID)

    def test_2(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertEqual(book_the_martian, book_record.book)

    def test_3(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertIsInstance(book_record.state, Available)

    def test_4(self):
        book_record = self.cut.add_book(book_the_martian)
        dispatched_event = self.cut.event_dispatcher.dispatch.call_args[0][0]
        self.assertIsInstance(dispatched_event, BookAdded)
        self.assertEqual(dispatched_event.book_record, book_record)
        self.assertIsInstance(dispatched_event.id, UUID)

    def test_9(self):
        self.cut.data_store = Mock()
        self.cut.data_store.create_or_update.side_effect = Exception
        with self.assertRaises(Exception):
            self.cut.add_book(book_the_martian)
        self.cut.event_dispatcher.dispatch.assert_not_called()


class TestBorrowBookFromCollection(TestCase):
    cut = None

    def test_happy_path(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Available())
        self.cut.data_store.find_by_id.return_value = book_record
        self.cut.data_store.create_or_update.return_value = book_record

        borrowed_book = self.cut.borrow_book(book_id, "Bob")

        self.assertIsInstance(borrowed_book.state, Borrowed)
        self.assertEqual(borrowed_book.state.by, "Bob")
        self.assertTrue(abs(datetime.now() - borrowed_book.state.on) < timedelta(seconds=1))

    def test_event(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Available())
        self.cut.data_store.find_by_id.return_value = book_record
        self.cut.data_store.create_or_update.return_value = book_record

        self.cut.borrow_book(book_id, "Bob")

        dispatched_event = self.cut.event_dispatcher.dispatch.call_args[0][0]
        self.assertIsInstance(dispatched_event, BookBorrowed)
        self.assertEqual(dispatched_event.book_record, book_record)
        self.assertIsInstance(dispatched_event.id, UUID)

    def test_bad(self):
        self.cut.data_store.find_by_id.side_effect = NotFoundException
        with self.assertRaises(NotFoundException):
            self.cut.borrow_book(uuid4(), "Bob")
        self.cut.event_dispatcher.dispatch.assert_not_called()

    def setUp(self):
        data_store = Mock()
        id_generator = BookIdGenerator(data_store)
        event_dispatcher = Mock()
        self.cut = BookCollection(id_generator, data_store, event_dispatcher)

    def test_scenraio2(self):
        book_id = uuid4()
        book_record = BookRecord(book_id, book_the_martian, Borrowed("Amy", datetime.now()))
        self.cut.data_store.find_by_id.return_value = book_record

        with self.assertRaises(AlreadyBorrowedException):
            borrowed_book = self.cut.borrow_book(book_id, "Bob")
        self.cut.event_dispatcher.dispatch.assert_not_called()

    def test_TODO(self):
        self.cut.data_store.find_by_id.side_effect = Exception

        with self.assertRaises(Exception):
            self.cut.borrow_book(uuid4(), "Bob")
        self.cut.event_dispatcher.dispatch.assert_not_called()


if __name__ == '__main__':
    main()
