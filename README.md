# claude-lab

Учебный проект: небольшой разбор строк с банковскими транзакциями на Java.
Он нужен не ради самого парсера, а как полигон для практики работы с Claude Code:
правки кода, тесты, git, ревью, подагенты, хуки и собственные скиллы.

## Что делает код

`TransactionParser.parse(String)` превращает строку вида

```
2026-10-26;Пятёрочка;1240.50
```

в запись `Transaction(LocalDate date, String merchant, BigDecimal amount)`.

Правила формата:

- **Дата**: строго `yyyy-MM-dd` (четыре цифры года, две цифры месяца и дня, только ASCII-цифры). Несуществующие даты вроде `2026-02-30` отклоняются.
- **Магазин**: не пустой, не длиннее 100 символов (считаются кодовые точки, поэтому эмодзи считается за один).
- **Сумма**: точка как разделитель, не более двух знаков после неё, может быть отрицательной (возврат), нулевой быть не может.
- Пробелы по краям полей обрезаются через `strip()`.

Для любого невалидного ввода, включая `null`, парсер бросает `TransactionParseException`
и ничего другого.

## Как отличить битую строку от других ошибок

`TransactionParseException` наследует `IllegalArgumentException`, но несёт больше информации:

- `getLine()` — исходная строка (`null`, если на входе был `null`);
- `getReason()` — машиночитаемая причина из enum `Reason`
  (`NULL_LINE`, `BLANK_LINE`, `WRONG_FIELD_COUNT`, `MISSING_DATE`, `INVALID_DATE`,
  `MISSING_MERCHANT`, `INVALID_MERCHANT`, `MISSING_AMOUNT`, `INVALID_AMOUNT`).

Это позволяет пропускать битые строки и собирать отчёт, не разбирая текст сообщения:

```java
for (String line : lines) {
    try {
        transactions.add(TransactionParser.parse(line));
    } catch (TransactionParseException e) {
        report.merge(e.getReason(), 1, Integer::sum);
    }
}
```

## Сборка и тесты

Нужны Java 21 и установленный Maven (Maven Wrapper в проекте нет).

```bash
mvn test
mvn test -Dtest=TransactionParserTest#parsesValidLine
```

## Что было сделано

1. **Своё исключение.** Вместо общего `IllegalArgumentException` с текстом появился
   `TransactionParseException` с причиной `Reason` и исходной строкой. Из нескольких
   вариантов выбран тот, что не ломает привычную обработку `IllegalArgumentException`.
2. **Ревью и ужесточение парсера.** Скилл `/code-review` нашёл места, где формат принимался
   шире документированного. Исправлено: строгий шаблон даты (расширенные годы ISO вроде
   `+12026-10-26` больше не проходят), `strip()` вместо `trim()`, запрет нулевой суммы.
3. **Тесты.** Параметризованная таблица невалидных строк, у каждой указана ожидаемая причина.
   Добавлены случаи с юникодными пробелами и цифрами, очень длинной суммой и длинным названием магазина.
4. **Проверка подагентом.** Подагент `Explore` независимо подтвердил, что `parse`
   не может бросить исключение другого типа.
5. **`CLAUDE.md`.** Описание команд и архитектуры для будущих сессий Claude Code.
6. **Хук `Stop`** ([.claude/settings.json](.claude/settings.json)). После ответа агента
   автоматически запускается `mvn -q test`, если менялись `src/` или `pom.xml`;
   результат показывается одной строкой. Хук использует `bash` и `mvn` из `PATH`.
7. **Скилл `add-parse-reason`** ([.claude/skills/add-parse-reason/SKILL.md](.claude/skills/add-parse-reason/SKILL.md)).
   По описанию нового вида битой строки делает три согласованные правки (значение в `Reason`,
   проверка в парсере, строка в тесте), прогоняет тесты и при необходимости обновляет `CLAUDE.md`.
   С его помощью добавлена проверка длины названия магазина.
8. **Памятка** ([docs/agents-cheatsheet.md](docs/agents-cheatsheet.md)) по работе с агентами:
   когда нужен подагент, что такое скиллы и хуки, рабочий цикл.

## Структура

```
src/main/java/ru/lab/
  Transaction.java               запись транзакции
  TransactionParser.java         разбор строки
  TransactionParseException.java исключение с причиной Reason
src/test/java/ru/lab/
  TransactionParserTest.java     тесты парсера
.claude/
  settings.json                  хук Stop
  skills/add-parse-reason/       проектный скилл
docs/agents-cheatsheet.md        памятка по работе с агентами
CLAUDE.md                        контекст проекта для Claude Code
```
