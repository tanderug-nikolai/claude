package ru.lab;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(LocalDate date, String merchant, BigDecimal amount) {
}
