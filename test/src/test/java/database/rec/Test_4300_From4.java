package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import me.legrange.typelink.Row4;
import org.junit.jupiter.api.TestTemplate;
import rec.Client;
import rec.Invoice;
import rec.Meeting;
import rec.Person;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.sum;

@SuppressWarnings({"NewClassNamingConvention", "unchecked"})
public final class Test_4300_From4 extends DatabaseTest {

    private static final String FROM = "FROM Invoice,Client,Person,Meeting "
            + "WHERE Invoice.clientId=Client.id AND Client.id=Person.clientId AND Client.id=Meeting.id";

    @TestTemplate
    public void testSelectToRecordWithConstructorReference(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.*,Person.*,Meeting.* " + FROM + " ORDER BY Invoice.id,Person.id",
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .orderBy((i, _, _, _) -> i.id())
                        .thenBy((_, _, p, _) -> p.id())
                        .list(All::new),
                (Row4<Invoice, Client, Person, Meeting> row) -> new All(row.v1(), row.v2(), row.v3(), row.v4()),
                Invoice.class, Client.class, Person.class, Meeting.class);
    }

    @TestTemplate
    public void testSelectToListOfRow(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.*,Person.*,Meeting.* " + FROM + " ORDER BY Invoice.id,Person.id",
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .orderBy((i, _, _, _) -> i.id())
                        .thenBy((_, _, p, _) -> p.id())
                        .list(),
                (Row4<Invoice, Client, Person, Meeting> row) -> row,
                Invoice.class, Client.class, Person.class, Meeting.class);
    }

    @TestTemplate
    public void testWhereAndOr(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT Invoice.id,Person.name " + FROM
                        + " AND Invoice.amount > 100 AND Person.name LIKE '%Bob%' ORDER BY Invoice.id",
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .and((i, _, _, _) -> i.amount() > 100)
                        .and((_, _, p, _) -> p.name().contains("Bob"))
                        .orderBy((i, _, _, _) -> i.id())
                        .list((i, _, _, _) -> i.id(), (_, _, p, _) -> p.name()),
                Long.class, String.class);
    }

    @TestTemplate
    public void testOrderByDescendingLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT Invoice.* " + FROM + " ORDER BY Invoice.amount DESC,Person.id LIMIT 4",
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .orderByDescending((i, _, _, _) -> i.amount())
                        .thenBy((_, _, p, _) -> p.id())
                        .limit(4)
                        .list((i, _, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testGroupByWithSum(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT Client.name,SUM(Invoice.amount) " + FROM + " GROUP BY Client.name ORDER BY SUM(Invoice.amount)",
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .groupBy((_, c, _, _) -> c.name())
                        .orderBy((i, _, _, _) -> sum(i.amount()))
                        .list((_, c, _, _) -> c.name(), (i, _, _, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testCount(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(*) " + FROM,
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .count(), Long.class);
    }

    @TestTemplate
    public void testSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(Invoice.amount) " + FROM,
                from(testDb, Invoice.class, Client.class, Person.class, Meeting.class)
                        .where((i, c, p, m) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()) && c.id().equals(m.id()))
                        .sum((i, _, _, _) -> i.amount()), Double.class);
    }

    private record All(Invoice invoice, Client client, Person person, Meeting meeting) {
    }
}
