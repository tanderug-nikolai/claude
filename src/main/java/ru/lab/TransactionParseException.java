package ru.lab;

/**
 * Thrown by {@link TransactionParser} when a line cannot be parsed. Carries the
 * offending line and a machine-readable {@link Reason}, so callers can skip bad
 * lines and group them in a report without parsing the message.
 */
public final class TransactionParseException extends IllegalArgumentException {

    public enum Reason {
        NULL_LINE,
        BLANK_LINE,
        WRONG_FIELD_COUNT,
        MISSING_DATE,
        INVALID_DATE,
        MISSING_MERCHANT,
        MISSING_AMOUNT,
        INVALID_AMOUNT,
        INVALID_MERCHANT
    }

    private final String line;
    private final Reason reason;

    public TransactionParseException(String line, Reason reason, String detail) {
        super(line == null ? "Невалидная транзакция: " + detail
                : "Невалидная транзакция \"%s\": %s".formatted(line, detail));
        this.line = line;
        this.reason = reason;
    }

    /** The input line as given to the parser; {@code null} if the input itself was null. */
    public String getLine() {
        return line;
    }

    public Reason getReason() {
        return reason;
    }
}
