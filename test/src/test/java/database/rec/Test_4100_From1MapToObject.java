package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import me.legrange.typelink.Row2;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.sum;

@SuppressWarnings({"NewClassNamingConvention", "unchecked"})
public final class Test_4100_From1MapToObject extends DatabaseTest {

    @TestTemplate
    public void testSelectAndMapToObject(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT Person.name,Person.email FROM Person ORDER BY Person.name", String.class, String.class)
                .stream()
                .map(row -> (Row2<String,String>)row)
                .map(row -> new NameAndEmail(row.v1(), row.v2()))
                .toList();
        var have = from(testDb, Person.class)
                .orderBy(Person::name)
                .list(p -> new NameAndEmail(p.name(), p.email()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSelectAndMapToObjectWithSum(TestDatabase testDb) throws SQLException {

        var have = from(testDb, Client.class)
                .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                .groupBy((c, i) -> c.id())
                .orderBy((c, i) -> c.name())
                .list((c, i) -> new ClientAndTotal(c, sum(i.amount())));
        var want = select(testDb, """
                SELECT Client.*, SUM(amount) FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                GROUP BY Client.id
                ORDER BY Client.name""", Client.class, Double.class)
                .stream()
                .map(row -> (Row2<Client,Double>)row)
                .map(row -> new ClientAndTotal(row.v1(), row.v2()))
                .toList();
        assertExpected(want, have);
    }

    private record NameAndEmail(String name, String email) {
    }

    private record ClientAndTotal(Client client, Double total) {}

}
