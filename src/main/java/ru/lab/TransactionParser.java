package ru.lab;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import ru.lab.TransactionParseException.Reason;

/**
 * Parses lines of the form {@code yyyy-MM-dd;merchant;amount}, e.g.
 * {@code 2026-10-26;Пятёрочка;1240.50}.
 *
 * <p>The date must be exactly four-digit year, two-digit month and day. The
 * amount uses a dot as the decimal separator, has at most two fractional
 * digits, may be negative (refunds) and must not be zero. Invalid input results in an
 * {@link TransactionParseException} describing what exactly is wrong.
 */
public final class TransactionParser {

    private static final char SEPARATOR = ';';
    private static final int FIELD_COUNT = 3;
    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern AMOUNT =Pattern.compile("-?\\d+(\\.\\d{1,2})?");

    private TransactionParser() {
    }

    public static Transaction parse(String line) {
        if (line == null) {
            throw error(null, Reason.NULL_LINE, "строка равна null");
        }
        if (line.isBlank()) {
            throw error(line, Reason.BLANK_LINE, "строка пустая");
        }

        String[] parts = line.split(String.valueOf(SEPARATOR), -1);
        if (parts.length != FIELD_COUNT) {
            throw error(line, Reason.WRONG_FIELD_COUNT, "ожидалось %d поля через '%c' (дата;магазин;сумма), найдено %d"
                    .formatted(FIELD_COUNT, SEPARATOR, parts.length));
        }

        return new Transaction(
                parseDate(line, parts[0].strip()),
                parseMerchant(line, parts[1].strip()),
                parseAmount(line, parts[2].strip()));
    }

    private static LocalDate parseDate(String line, String raw) {
        if (raw.isEmpty()) {
            throw error(line, Reason.MISSING_DATE, "дата не указана");
        }
        if (!DATE.matcher(raw).matches()) {
            throw error(line, Reason.INVALID_DATE, "некорректная дата '%s', ожидался формат yyyy-MM-dd"
                    .formatted(raw));
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw error(line, Reason.INVALID_DATE, "некорректная дата '%s', ожидался формат yyyy-MM-dd (%s)"
                    .formatted(raw, e.getMessage()));
        }
    }

    private static String parseMerchant(String line, String raw) {
        if (raw.isEmpty()) {
            throw error(line, Reason.MISSING_MERCHANT, "название магазина не указано");
        }
        return raw;
    }

    private static BigDecimal parseAmount(String line, String raw) {
        if (raw.isEmpty()) {
            throw error(line, Reason.MISSING_AMOUNT, "сумма не указана");
        }
        if (!AMOUNT.matcher(raw).matches()) {
            throw error(line, Reason.INVALID_AMOUNT, "некорректная сумма '%s', ожидалось число с точкой и не более 2 знаков после неё"
                    .formatted(raw));
        }
        BigDecimal amount = new BigDecimal(raw);
        if (amount.signum() == 0) {
            throw error(line, Reason.INVALID_AMOUNT, "сумма не может быть нулевой");
        }
        return amount;
    }

    private static TransactionParseException error(String line, Reason reason, String detail) {
        return new TransactionParseException(line, reason, detail);
    }
}
