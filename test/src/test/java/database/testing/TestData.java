package database.testing;

import rec.Client;
import rec.Invoice;
import rec.Meeting;
import rec.Person;
import rec.Town;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static rec.Person.Sex.FEMALE;
import static rec.Person.Sex.MALE;

public final class TestData {

    public static List<Town> towns = List.of(
            new Town(1L, "Cape Town", 25f),
            new Town(2L, "Stellenbosch", 136f),
            new Town(3L, "Paarl", 120f),
            new Town(4L, "Worcester", 220f),
            new Town(5L, "George", 195f),
            new Town(6L, "Hermanus", 17f),
            new Town(7L, "Oudtshoorn", 307f),
            new Town(8L, "Ceres", 457f),
            new Town(9L, "Beaufort West", 825f),
            new Town(10L, "Swellendam", 128f),
            new Town(11L, "Montagu", 228f),
            new Town(12L, "Vredenburg", 133f),
            new Town(13L, "Saldanha", 55f),
            new Town(14L, "Langebaan", 34f),
            new Town(15L, "Malmesbury", 114f),
            new Town(16L, "Clanwilliam", 100f),
            new Town(17L, "Citrusdal", 169f),
            new Town(18L, "Prince Albert", 619f),
            new Town(19L, "Ladismith", 550f),
            new Town(20L, "Robertson", 192f),
            new Town(21L, "Tulbagh", 168f)
    );
    static List<Client> clients = List.of(
            new Client(1L, "Acme Corp"),
            new Client(2L, "Coyote Inc"),
            new Client(3L, "FooBar Industries"),
            new Client(4L, "Acme Industries")
    );
    static List<Person> persons = List.of(
            new Person(1L, 1L, "Bob Jones", 30, "bob@acme.com", MALE, LocalDate.parse("1994-05-11")),
            new Person(2L, 2L, "Ryan Smith", 18, "ryan@acme.com", MALE, LocalDate.parse("1994-05-11")),
            new Person(3L, 2L, "Didi Rhyder", 69, "didnot@coyote.com", FEMALE, LocalDate.parse("1955-12-31")),
            new Person(4L, 0L, "Lil Joe Johns", 11, "", MALE, LocalDate.parse("2014-01-03")),
            new Person(5L, 2L, "Bob Jones", 20, "bjones@coyote.com", MALE, LocalDate.parse("2004-01-23")),
            new Person(6L, 1L, "Alice Smith", 25, "alice@coyote.com", FEMALE, null),
            new Person(7L, 0L, "Jane Doe", 33, null, FEMALE, null),
            new Person(8L, 3L, "Frans Fubar", 69, "frans@fubar.com", MALE, null),
            new Person(9L, 4L, "Rhode Runner", 75, "rhode@runner.com", MALE, LocalDate.parse("1950-12-27")),
            new Person(10L, 0L, "Bob Bobberton (Retired)", 75, null, MALE, LocalDate.parse("1950-12-11"))
            );
    static List<Invoice> invoices = List.of(
            new Invoice(1L, 1L, LocalDate.parse("2024-10-01"), "Invoice #1", 100.0),
            new Invoice(2L, 2L, LocalDate.parse("2024-10-01"), "Invoice #2", 200, true),
            new Invoice(3L, 3L, LocalDate.parse("2024-10-01"), "Invoice #3", 300),
            new Invoice(4L, 1L, LocalDate.parse("2024-11-01"), "Invoice #4", 400, true),
            new Invoice(5L, 2L, LocalDate.parse("2024-11-01"), "Invoice #5", 500.0),
            new Invoice(6L, 3L, LocalDate.parse("2024-11-01"), "Invoice #6", 600, true),
            new Invoice(7L, 1L, LocalDate.parse("2024-12-01"), "Invoice #7", 700.0, true),
            new Invoice(8L, 2L, LocalDate.parse("2024-12-01"), "Invoice #8", 800.00),
            new Invoice(9L, 3L, LocalDate.parse("2024-12-01"), "Invoice #9", 900.00),
            new Invoice(10L, 1L, LocalDate.parse("2025-06-01"), "Invoice #10", 50.00),
            new Invoice(11L, 3L, LocalDate.parse("2025-06-01"), "Invoice #11", 1050.00),
            new Invoice(12L, 4L, LocalDate.parse("2025-06-01"), "Invoice #12", 750.00)
    );

    static List<Meeting> meetings = List.of(
            new Meeting(1L, "Meeting with Acme Industries", LocalDateTime.of(2026, 5, 11, 9, 0),
                    LocalDateTime.of(2026, 5, 14, 10, 0)),
            new Meeting(2L, "Meeting with Acme Corp", LocalDateTime.of(2026, 5, 11, 10, 0),
                    LocalDateTime.of(2026, 5, 14, 11, 0)),
            new Meeting(3L, "Meeting with Coyote Inc", LocalDateTime.of(2026, 5, 11, 11, 0),
                    LocalDateTime.of(2026, 5, 14, 12, 0)),
            new Meeting(4L, "Unscheduled meeting with Coyote Inc", null, null)
    );

    static void populateData(Supplier<Connection> connectionSupplier) throws SQLException, InvocationTargetException, IllegalAccessException {
        populateTable(connectionSupplier, Person.class, persons);
        populateTable(connectionSupplier, Invoice.class, invoices);
        populateTable(connectionSupplier, Client.class, clients);
        populateTable(connectionSupplier, Town.class, towns);
        populateTable(connectionSupplier, Meeting.class, meetings);
    }

    private static <T> void populateTable(Supplier<Connection> connectionSupplier, Class<T> type, List<T> data) throws SQLException, InvocationTargetException, IllegalAccessException {
        try (var con = connectionSupplier.get(); var stmt = con.createStatement(); var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + type.getSimpleName())) {
            if (rs.next()) {
                var count = rs.getInt(1);
                if (count > 0) {
                    return;
                }
            }
            for (var obj : data) {
                stmt.executeUpdate(makeCreateStatement(obj));
            }
        }
    }

    private static String makeCreateStatement(Object obj) throws InvocationTargetException, IllegalAccessException {
        String sql;
        if (obj instanceof Record) {
            sql = "INSERT INTO " + obj.getClass().getSimpleName() + "("
                    + Arrays.stream(obj.getClass().getRecordComponents())
                    .map(RecordComponent::getName).collect(Collectors.joining(","))
                    + ") VALUES(";
            var values = new ArrayList<String>();
            for (var comp : obj.getClass().getRecordComponents()) {
                var val = comp.getAccessor().invoke(obj);
                values.add(format(val));
            }
            return sql + String.join(",", values) + ")";
        } else {
            sql = "INSERT INTO " + obj.getClass().getSimpleName() + "("
                    + Arrays.stream(obj.getClass().getDeclaredFields())
                    .map(Field::getName).collect(Collectors.joining(","))
                    + ") VALUES(";
            var values = new ArrayList<String>();
            for (var comp : obj.getClass().getDeclaredFields()) {
                comp.setAccessible(true);
                var val = comp.get(obj);
                values.add(format(val));
            }
            return sql + String.join(",", values) + ")";
        }
    }

    private static String format(Object val) {
        return switch (val) {
            case String s -> "'" + s + "'";
            case Integer i -> i.toString();
            case Long l -> l.toString();
            case Boolean b -> b.toString();
            case Double d -> d.toString();
            case Float f -> f.toString();
            case LocalDate ld -> "'" + ld.format(DateTimeFormatter.ISO_DATE) + "'";
            case null -> "NULL";
            default -> "'" + val + "'";
        };
    }

}
