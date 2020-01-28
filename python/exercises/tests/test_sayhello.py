import unittest
from library.books import BookIdGenerator


class TestSayHello(unittest.TestCase):

    def test_each_invocation_generates_new_id(self):
        cut = BookIdGenerator()
        id1 = cut.generate()
        id2 = cut.generate()
        self.assertNotEqual(id1, id2)


if __name__ == '__main__':
    unittest.main()
