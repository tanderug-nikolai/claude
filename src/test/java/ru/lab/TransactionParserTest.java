package ru.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.lab.TransactionParseException.Reason;

class TransactionParserTest {

    @Test
    void parsesValidLine() {
        Transaction t = TransactionParser.parse("2026-10-26;Пятёрочка;1240.50");

        assertEquals(new Transaction(LocalDate.of(2026, 10, 26), "Пятёрочка", new BigDecimal("1240.50")), t);
    }

    @Test
    void trimsFieldsAndAcceptsNegativeAmount() {
        Transaction t = TransactionParser.parse(" 2026-10-26 ; Лента ; -15 ");

        assertEquals("Лента", t.merchant());
        assertEquals(new BigDecimal("-15"), t.amount());
    }

    static Stream<Arguments> invalidLines() {
        return Stream.of(
                Arguments.of(null, Reason.NULL_LINE),
                Arguments.of("", Reason.BLANK_LINE),
                Arguments.of("   ", Reason.BLANK_LINE),
                Arguments.of("2026-10-26;Лента", Reason.WRONG_FIELD_COUNT),
                Arguments.of("2026-10-26;Лента;10;extra", Reason.WRONG_FIELD_COUNT),
                Arguments.of("26.10.2026;Лента;10", Reason.INVALID_DATE),
                Arguments.of("2026-02-30;Лента;10", Reason.INVALID_DATE),
                Arguments.of("+12026-10-26;Лента;10", Reason.INVALID_DATE),
                Arguments.of("-0001-01-01;Лента;10", Reason.INVALID_DATE),
                Arguments.of("2026-1-5;Лента;10", Reason.INVALID_DATE),
                Arguments.of(";Лента;10", Reason.MISSING_DATE),
                Arguments.of(";;", Reason.MISSING_DATE),
                Arguments.of("　;Лента;10", Reason.MISSING_DATE),
                Arguments.of("　", Reason.BLANK_LINE),
                Arguments.of("2026-10-26; ;10", Reason.MISSING_MERCHANT),
                Arguments.of("2026-10-26;　;10", Reason.MISSING_MERCHANT),
                Arguments.of("2026-10-26;Лента;", Reason.MISSING_AMOUNT),
                Arguments.of("2026-10-26;Лента;　", Reason.MISSING_AMOUNT),
                Arguments.of("2026-10-26;Лента;12,50", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;1e3", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;10.123", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;abc", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;-", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;0", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;0.00", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;-0", Reason.INVALID_AMOUNT),
                Arguments.of("2026-10-26;Лента;-0.00", Reason.INVALID_AMOUNT));
    }

    @ParameterizedTest
    @MethodSource("invalidLines")
    void rejectsInvalidLines(String line, Reason expectedReason) {
        TransactionParseException e = assertThrows(TransactionParseException.class, () -> TransactionParser.parse(line));

        assertEquals(expectedReason, e.getReason());
        assertEquals(line, e.getLine());
        assertTrue(e.getMessage().startsWith("Невалидная транзакция"), e.getMessage());
    }

    @Test
    void parseExceptionIsStillAnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> TransactionParser.parse("мусор"));
    }
}
