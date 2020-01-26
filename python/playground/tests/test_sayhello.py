import unittest
from playground.sayhello import say_hello


class TestSayHello(unittest.TestCase):

    def test_message_is_created_for_name(self):
        self.assertEqual(say_hello("Stefan"), "Hello Stefan!")
        self.assertEqual(say_hello("Alexander"), "Hello Alexander!")

    def test_message_is_created_for_empty_input(self):
        self.assertEqual(say_hello(""), "Hello World!")


if __name__ == '__main__':
    unittest.main()
