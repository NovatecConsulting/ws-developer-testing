def roman_numeral_to_number(num: str):
    res = 0
    last = ''
    if num == '':
        return 0

    for c in num.upper():
        if c == "I":
            res += 1
        elif c == "V":
            res += 3 if last == "I" else 5
        elif c == "X":
            res += 8 if last == "I" else 10
        elif c == "L":
            res += 30 if last == "X" else 50
        last = c

    return res
