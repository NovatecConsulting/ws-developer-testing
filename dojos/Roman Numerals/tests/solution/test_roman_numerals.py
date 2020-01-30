import unittest
from roman_numerals.solution.roman_numerals import roman_numeral_to_number


class TestRomanNumerals(unittest.TestCase):

    def test_I_is_1(self):
        self.assertEqual(roman_numeral_to_number("I"), 1)

    def test_II_is_2(self):
        self.assertEqual(roman_numeral_to_number("II"), 2)

    def test_III_is_3(self):
        self.assertEqual(roman_numeral_to_number("III"), 3)

    def test_IV_is_4(self):
        self.assertEqual(roman_numeral_to_number("IV"), 4)

    def test_V_is_5(self):
        self.assertEqual(roman_numeral_to_number("V"), 5)

    def test_VI_is_6(self):
        self.assertEqual(roman_numeral_to_number("VI"), 6)

    def test_VII_is_7(self):
        self.assertEqual(roman_numeral_to_number("VII"), 7)

    def test_VIII_is_8(self):
        self.assertEqual(roman_numeral_to_number("VIII"), 8)

    def test_IX_is_9(self):
        self.assertEqual(roman_numeral_to_number("IX"), 9)

    def test_XX_is_20(self):
        self.assertEqual(roman_numeral_to_number("XX"), 20)

    def test_XXXIX_is_39(self):
        self.assertEqual(roman_numeral_to_number("XXXIX"), 39)

    def test_L_is_50(self):
        self.assertEqual(roman_numeral_to_number("L"), 50)

    def test_XL_is_40(self):
        self.assertEqual(roman_numeral_to_number("XL"), 40)

    def test_XLIX_is_49(self):
        self.assertEqual(roman_numeral_to_number("XLIX"), 49)


if __name__ == '__main__':
    unittest.main()
