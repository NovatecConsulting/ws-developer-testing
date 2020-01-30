import unittest
from roman_numerals.dojo.roman_numerals import roman_numeral_to_number


class TestRomanNumerals(unittest.TestCase):

    def test_I_is_1(self):
        self.assertEqual(roman_numeral_to_number("I"), 1)


if __name__ ==  '__main__':
    unittest.main()
