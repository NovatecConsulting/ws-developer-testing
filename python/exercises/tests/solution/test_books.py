from library.books import *
from tests.testdata import *

from unittest import TestCase, main
from unittest.mock import Mock, patch


class TestBookIdGenerator(TestCase):

    def test_each_invocation_generates_new_id(self):
        cut = BookIdGenerator(BookDataStore())
        id1 = cut.generate()
        id2 = cut.generate()
        self.assertNotEqual(id1, id2)


class TestAddBookToCollection(TestCase):

    cut = None

    def setUp(self):
        data_store = BookDataStore()
        id_generator = BookIdGenerator(data_store)
        event_dispatcher = Mock()
        self.cut = BookCollection(id_generator, data_store, event_dispatcher)

    def test_adding_a_book_generates_a_new_book_id(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertIsInstance(book_record.id, UUID)

    def test_adding_a_book_stores_the_books_data(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertEqual(book_the_martian, book_record.book)

    def test_adding_a_book_sets_its_initial_state_to_available(self):
        book_record = self.cut.add_book(book_the_martian)
        self.assertIsInstance(book_record.state, Available)

    def test_adding_a_book_dispatches_a_BookAdded_event(self):
        book_record = self.cut.add_book(book_the_martian)
        dispatched_event = self.cut.event_dispatcher.dispatch.call_args[0][0]
        self.assertIsInstance(dispatched_event, BookAdded)
        self.assertEqual(dispatched_event.book_record, book_record)
        self.assertIsInstance(dispatched_event.id, UUID)

    @patch("library.books.uuid4")
    def test_adding_a_book_dispatches_a_BookAdded_event__with_patching(self, mock_uuid4):
        mock_uuid4.return_value = UUID("1e5f898d-6976-4560-901f-6abd0f0492c9")
        book_record = self.cut.add_book(book_the_martian)
        expected_event = BookAdded(mock_uuid4.return_value, book_record)
        self.cut.event_dispatcher.dispatch.assert_called_with(expected_event)

    def test_adding_a_book_does_not_dispatch_any_events_in_case_of_an_error(self):
        self.cut.data_store = Mock()
        self.cut.data_store.create_or_update.side_effect = Exception
        with self.assertRaises(Exception):
            self.cut.add_book(book_the_martian)
        self.cut.event_dispatcher.dispatch.assert_not_called()


if __name__ == '__main__':
    main()
