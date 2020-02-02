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


if __name__ == '__main__':
    main()
