# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Учебный проект: разбор строк с банковскими транзакциями. Java 21, Maven, JUnit 5. Внешних клиентов у кода нет, поэтому обратную совместимость API сохранять не обязательно.

## Команды

В репозитории нет Maven Wrapper, нужен установленный `mvn`.

```bash
mvn test                                                    # все тесты
mvn test -Dtest=TransactionParserTest                       # один класс
mvn test -Dtest=TransactionParserTest#parsesValidLine       # один тест
mvn test -Dtest=TransactionParserTest#rejectsInvalidLines   # параметризованный тест целиком
```

Линтеров и форматтеров в проекте не настроено.

## Архитектура

Пакет `ru.lab`, три класса в `src/main/java`:

- `TransactionParser.parse(String)` — статический разбор строки формата `yyyy-MM-dd;магазин;сумма` (например `2026-10-26;Пятёрочка;1240.50`). Поля обрезаются по краям; сумма с точкой, не более двух знаков после неё, может быть отрицательной (возврат).
- `Transaction` — `record(LocalDate date, String merchant, BigDecimal amount)`. Валидации в нём нет, вся проверка живёт в парсере.
- `TransactionParseException` — единственный тип ошибки парсера. Наследует `IllegalArgumentException`, несёт исходную строку (`getLine()`, `null` если на входе был `null`) и машиночитаемую причину `getReason()` (вложенный enum `Reason`). Это нужно, чтобы вызывающий код мог пропускать битые строки и группировать ошибки в отчёте, не разбирая текст сообщения.

Как связаны части: каждый `throw` в `TransactionParser` идёт через приватный `error(line, Reason, detail)`, поэтому новый вид ошибки требует трёх согласованных изменений: значение в `Reason`, `throw` с этим значением в парсере и строка в `invalidLines()` в `TransactionParserTest`.

## Нюансы

- Сообщения об ошибках на русском и начинаются с `Невалидная транзакция`; тест проверяет этот префикс.
- Парсер не должен бросать другие исключения для невалидного ввода, в том числе для `null`: все случаи покрыты значениями `Reason`.
- `tryParse`/результат без исключений сознательно не добавлялся; парсер только бросает исключения.
- `target/` и `.idea/` в `.gitignore`, в git не попадают.
