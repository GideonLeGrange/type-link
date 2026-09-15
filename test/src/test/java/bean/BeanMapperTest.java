package bean;

import me.legrange.typelink.BeanMapper;
import me.legrange.typelink.TableMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify BeanMapper works correctly with Java bean-style POJOs.
 */
class BeanMapperTest {

    @Test
    void testClientMapping() {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        // Verify it recognizes Client as a table
        assertTrue(mapper.isTable(Client.class));

        // Verify table name
        assertEquals("Client", mapper.tableName(Client.class));

        // Verify column names
        List<String> columnNames = mapper.columnNames(Client.class);
        assertTrue(columnNames.contains("id"));
        assertTrue(columnNames.contains("name"));
        assertEquals(2, columnNames.size());

        // Verify column types
        assertEquals(Long.class, mapper.columnType(Client.class, "id"));
        assertEquals(String.class, mapper.columnType(Client.class, "name"));

        // Test assembly
        Map<String, Object> values = new HashMap<>();
        values.put("id", 1L);
        values.put("name", "John Doe");

        Client client = (Client) mapper.assemble(Client.class, values);
        assertNotNull(client);
        assertEquals(1L, client.getId());
        assertEquals("John Doe", client.getName());
    }

    @Test
    void testPersonMapping() {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        // Verify column names
        List<String> columnNames = mapper.columnNames(Person.class);
        assertTrue(columnNames.contains("id"));
        assertTrue(columnNames.contains("clientId"));
        assertTrue(columnNames.contains("name"));
        assertTrue(columnNames.contains("age"));
        assertTrue(columnNames.contains("email"));
        assertTrue(columnNames.contains("sex"));
        assertTrue(columnNames.contains("birthDay"));

        // Test assembly with enum
        Map<String, Object> values = new HashMap<>();
        values.put("id", 10L);
        values.put("clientId", 5L);
        values.put("name", "Jane Smith");
        values.put("age", 30);
        values.put("email", "jane@example.com");
        values.put("sex", "FEMALE");
        values.put("birthDay", LocalDate.of(1993, 5, 15));

        Person person = (Person) mapper.assemble(Person.class, values);
        assertNotNull(person);
        assertEquals(10L, person.getId());
        assertEquals(5L, person.getClientId());
        assertEquals("Jane Smith", person.getName());
        assertEquals(30, person.getAge());
        assertEquals("jane@example.com", person.getEmail());
        assertEquals(Person.Sex.FEMALE, person.getSex());
        assertEquals(LocalDate.of(1993, 5, 15), person.getBirthDay());
    }

    @Test
    void testInvoiceMapping() {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        Map<String, Object> values = new HashMap<>();
        values.put("id", 100L);
        values.put("clientId", 5L);
        values.put("invoiceDate", LocalDate.of(2024, 1, 15));
        values.put("description", "Consulting services");
        values.put("amount", 1500.50);
        values.put("paid", true);

        Invoice invoice = (Invoice) mapper.assemble(Invoice.class, values);
        assertNotNull(invoice);
        assertEquals(100L, invoice.getId());
        assertEquals(5L, invoice.getClientId());
        assertEquals(LocalDate.of(2024, 1, 15), invoice.getInvoiceDate());
        assertEquals("Consulting services", invoice.getDescription());
        assertEquals(1500.50, invoice.getAmount());
        assertTrue(invoice.isPaid());
    }

    @Test
    void testMeetingMapping() {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        Map<String, Object> values = new HashMap<>();
        values.put("id", 50L);
        values.put("subject", "Project Review");
        values.put("startTime", LocalDateTime.of(2024, 3, 20, 14, 0));
        values.put("endTime", LocalDateTime.of(2024, 3, 20, 15, 30));

        Meeting meeting = (Meeting) mapper.assemble(Meeting.class, values);
        assertNotNull(meeting);
        assertEquals(50L, meeting.getId());
        assertEquals("Project Review", meeting.getSubject());
        assertEquals(LocalDateTime.of(2024, 3, 20, 14, 0), meeting.getStartTime());
        assertEquals(LocalDateTime.of(2024, 3, 20, 15, 30), meeting.getEndTime());
    }

    @Test
    void testTownMapping() {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        Map<String, Object> values = new HashMap<>();
        values.put("id", 25L);
        values.put("name", "Springfield");
        values.put("alt", 250.5f);

        Town town = (Town) mapper.assemble(Town.class, values);
        assertNotNull(town);
        assertEquals(25L, town.getId());
        assertEquals("Springfield", town.getName());
        assertEquals(250.5f, town.getAlt());
    }

    @Test
    void testGetterMethodIsColumn() throws NoSuchMethodException {
        TableMapper<Bean> mapper = new BeanMapper<>(Bean.class);

        // Test that getter methods are recognized as columns
        assertTrue(mapper.isColumn(Client.class.getMethod("getId")));
        assertTrue(mapper.isColumn(Client.class.getMethod("getName")));
        assertTrue(mapper.isColumn(Invoice.class.getMethod("isPaid")));

        // Test that non-getter methods are not columns
        assertFalse(mapper.isColumn(Client.class.getMethod("setId", Long.class)));
    }
}

