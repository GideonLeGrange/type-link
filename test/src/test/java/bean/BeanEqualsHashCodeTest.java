package bean;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify equals() and hashCode() methods work correctly on bean classes.
 */
class BeanEqualsHashCodeTest {

    @Test
    void testClientEquals() {
        Client c1 = new Client(1L, "John Doe");
        Client c2 = new Client(1L, "John Doe");
        Client c3 = new Client(2L, "Jane Smith");
        Client c4 = new Client(1L, "Jane Smith");

        // Reflexive: object equals itself
        assertEquals(c1, c1);

        // Symmetric: if a equals b, then b equals a
        assertEquals(c1, c2);
        assertEquals(c2, c1);

        // Different objects are not equal
        assertNotEquals(c1, c3);
        assertNotEquals(c1, c4);

        // Null check
        assertNotEquals(c1, null);

        // Different type check
        assertNotEquals(c1, "Not a Client");
    }

    @Test
    void testClientHashCode() {
        Client c1 = new Client(1L, "John Doe");
        Client c2 = new Client(1L, "John Doe");
        Client c3 = new Client(2L, "Jane Smith");

        // Equal objects must have equal hash codes
        assertEquals(c1.hashCode(), c2.hashCode());

        // Different objects should (ideally) have different hash codes
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    void testPersonEquals() {
        Person p1 = new Person(10L, 5L, "Alice", 30, "alice@example.com", Person.Sex.FEMALE, LocalDate.of(1993, 5, 15));
        Person p2 = new Person(10L, 5L, "Alice", 30, "alice@example.com", Person.Sex.FEMALE, LocalDate.of(1993, 5, 15));
        Person p3 = new Person(11L, 5L, "Bob", 25, "bob@example.com", Person.Sex.MALE, LocalDate.of(1998, 3, 10));
        Person p4 = new Person(10L, 5L, "Alice", 31, "alice@example.com", Person.Sex.FEMALE, LocalDate.of(1993, 5, 15));

        // Reflexive
        assertEquals(p1, p1);

        // Symmetric
        assertEquals(p1, p2);
        assertEquals(p2, p1);

        // Different persons
        assertNotEquals(p1, p3);
        assertNotEquals(p1, p4); // Different age

        // Null check
        assertNotEquals(p1, null);
    }

    @Test
    void testPersonHashCode() {
        Person p1 = new Person(10L, 5L, "Alice", 30, "alice@example.com", Person.Sex.FEMALE, LocalDate.of(1993, 5, 15));
        Person p2 = new Person(10L, 5L, "Alice", 30, "alice@example.com", Person.Sex.FEMALE, LocalDate.of(1993, 5, 15));
        Person p3 = new Person(11L, 5L, "Bob", 25, "bob@example.com", Person.Sex.MALE, LocalDate.of(1998, 3, 10));

        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }

    @Test
    void testInvoiceEquals() {
        Invoice i1 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 1500.50, true);
        Invoice i2 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 1500.50, true);
        Invoice i3 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 1500.50, false); // Different paid status
        Invoice i4 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 2000.00, true); // Different amount

        // Reflexive
        assertEquals(i1, i1);

        // Symmetric
        assertEquals(i1, i2);
        assertEquals(i2, i1);

        // Different invoices
        assertNotEquals(i1, i3); // Different paid status
        assertNotEquals(i1, i4); // Different amount

        // Null check
        assertNotEquals(i1, null);
    }

    @Test
    void testInvoiceHashCode() {
        Invoice i1 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 1500.50, true);
        Invoice i2 = new Invoice(100L, 5L, LocalDate.of(2024, 1, 15), "Consulting", 1500.50, true);
        Invoice i3 = new Invoice(101L, 6L, LocalDate.of(2024, 2, 20), "Development", 2500.00, false);

        assertEquals(i1.hashCode(), i2.hashCode());
        assertNotEquals(i1.hashCode(), i3.hashCode());
    }

    @Test
    void testMeetingEquals() {
        Meeting m1 = new Meeting(50L, "Project Review", LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 15, 30));
        Meeting m2 = new Meeting(50L, "Project Review", LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 15, 30));
        Meeting m3 = new Meeting(51L, "Team Sync", LocalDateTime.of(2024, 3, 21, 10, 0), LocalDateTime.of(2024, 3, 21, 11, 0));
        Meeting m4 = new Meeting(50L, "Project Review", LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 16, 0)); // Different end time

        // Reflexive
        assertEquals(m1, m1);

        // Symmetric
        assertEquals(m1, m2);
        assertEquals(m2, m1);

        // Different meetings
        assertNotEquals(m1, m3);
        assertNotEquals(m1, m4);

        // Null check
        assertNotEquals(m1, null);
    }

    @Test
    void testMeetingHashCode() {
        Meeting m1 = new Meeting(50L, "Project Review", LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 15, 30));
        Meeting m2 = new Meeting(50L, "Project Review", LocalDateTime.of(2024, 3, 20, 14, 0), LocalDateTime.of(2024, 3, 20, 15, 30));
        Meeting m3 = new Meeting(51L, "Team Sync", LocalDateTime.of(2024, 3, 21, 10, 0), LocalDateTime.of(2024, 3, 21, 11, 0));

        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1.hashCode(), m3.hashCode());
    }

    @Test
    void testTownEquals() {
        Town t1 = new Town(25L, "Springfield", 250.5f);
        Town t2 = new Town(25L, "Springfield", 250.5f);
        Town t3 = new Town(26L, "Shelbyville", 180.0f);
        Town t4 = new Town(25L, "Springfield", 251.0f); // Different altitude

        // Reflexive
        assertEquals(t1, t1);

        // Symmetric
        assertEquals(t1, t2);
        assertEquals(t2, t1);

        // Different towns
        assertNotEquals(t1, t3);
        assertNotEquals(t1, t4);

        // Null check
        assertNotEquals(t1, null);
    }

    @Test
    void testTownHashCode() {
        Town t1 = new Town(25L, "Springfield", 250.5f);
        Town t2 = new Town(25L, "Springfield", 250.5f);
        Town t3 = new Town(26L, "Shelbyville", 180.0f);

        assertEquals(t1.hashCode(), t2.hashCode());
        assertNotEquals(t1.hashCode(), t3.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        // Test equals with null fields
        Client c1 = new Client(null, null);
        Client c2 = new Client(null, null);
        Client c3 = new Client(1L, null);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);

        Person p1 = new Person(null, null, null, 0, null, null, null);
        Person p2 = new Person(null, null, null, 0, null, null, null);
        Person p3 = new Person(1L, null, null, 0, null, null, null);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

    @Test
    void testTransitiveProperty() {
        // Transitive: if a equals b and b equals c, then a equals c
        Client a = new Client(1L, "Test");
        Client b = new Client(1L, "Test");
        Client c = new Client(1L, "Test");

        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    void testConsistency() {
        // Multiple invocations should return consistent results
        Person p1 = new Person(1L, 2L, "Test", 25, "test@example.com", Person.Sex.MALE, LocalDate.of(1999, 1, 1));
        Person p2 = new Person(1L, 2L, "Test", 25, "test@example.com", Person.Sex.MALE, LocalDate.of(1999, 1, 1));

        // Multiple calls to equals should return the same result
        for (int i = 0; i < 5; i++) {
            assertTrue(p1.equals(p2));
            assertEquals(p1.hashCode(), p2.hashCode());
        }
    }
}

