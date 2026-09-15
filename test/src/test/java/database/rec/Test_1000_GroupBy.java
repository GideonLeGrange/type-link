package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDate;

import static me.legrange.typelink.Selects.count;
import static me.legrange.typelink.Selects.sum;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_1000_GroupBy extends DatabaseTest {

    @TestTemplate
    public void testGroupByWithSumAndCount(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT clientId,SUM(amount),count(Invoice.id) FROM Invoice GROUP BY clientId", from(testDb, Invoice.class)
                        .groupBy(Invoice::clientId)
                        .list(Invoice::clientId, i -> sum(i.amount()), i -> count(i.id())),
                Long.class, Double.class, Long.class);
    }

    @TestTemplate
    public void testGroupByGroupByWithSum(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT clientId,invoiceDate,SUM(amount) FROM Invoice GROUP BY clientId, invoiceDate", from(testDb, Invoice.class)
                        .groupBy(Invoice::clientId, Invoice::invoiceDate)
                        .list(Invoice::clientId, Invoice::invoiceDate, i -> sum(i.amount())),
                Long.class, LocalDate.class, Double.class);
    }

    @TestTemplate
    public void testJoinWithGroupByAndSum(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Client INNER JOIN Invoice ON Client.id=Invoice.clientId
                        GROUP BY Client.name""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .groupBy((c, _) -> c.name())
                        .list((c, _) -> c.name(), (_, i) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testJoinWithGroupByAndSumWithMath(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount*2) FROM Client INNER JOIN Invoice ON Client.id=Invoice.clientId
                        GROUP BY Client.name""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .groupBy((c, _) -> c.name())
                        .list((c, _) -> c.name(), (_, i) -> sum(i.amount() * 2)),
                String.class, Double.class);
    }

    @TestTemplate
    public void testJoinWithGroupByAndCount(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,COUNT(*) FROM Client INNER JOIN Invoice ON Client.id=Invoice.clientId
                        GROUP BY Client.name""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .groupBy((c, _) -> c.name())
                        .list((c, _) -> c.name(), (c, _) -> count(c)),
                String.class, Long.class);
    }


    @TestTemplate
    public void testJoinJoinGroupBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Client
                        INNER JOIN Invoice ON Client.id=Invoice.clientId
                        INNER JOIN Person ON Client.id=Person.clientId
                        GROUP BY Client.name""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .join(Person.class, (c, _, p) -> c.id().equals(p.clientId()))
                        .groupBy((c, _, _) -> c.name())
                        .list((c, _, _) -> c.name(), (_, i, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testJoinGroupByFilter(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Client INNER JOIN Invoice ON Client.id=Invoice.clientId
                        WHERE Client.name LIKE 'Acme%'
                        GROUP BY Client.name""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .where((c, _) -> c.name().startsWith("Acme"))
                        .groupBy((c, _) -> c.name())
                        .list((c, _) -> c.name(), (_, i) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testGroupByMultipleFields(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,Invoice.invoiceDate,SUM(Invoice.amount) FROM Client INNER JOIN Invoice ON Client.id=Invoice.clientId
                        GROUP BY Client.name, Invoice.invoiceDate""", from(testDb, Client.class)
                        .join(Invoice.class, (c, i) -> c.id().equals(i.clientId()))
                        .groupBy((c, _) -> c.name(), (_, i) -> i.invoiceDate())
                        .list((c, _) -> c.name(), (_, i) -> i.invoiceDate(), (_, i) -> sum(i.amount())),
                String.class, LocalDate.class, Double.class);
    }

}
