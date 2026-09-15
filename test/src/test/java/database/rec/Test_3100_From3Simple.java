package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import me.legrange.typelink.Row3;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings({"NewClassNamingConvention", "unchecked"})
public final class Test_3100_From3Simple extends DatabaseTest {

    @TestTemplate
    public void testSelectToRecordWithConstructorReference(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.*,Person.* FROM Invoice,Client,Person WHERE Invoice.clientId=Client.id AND Client.id=Person.clientId",
                from(testDb, Invoice.class, Client.class, Person.class)
                        .where((i, c, p) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()))
                        .list(InvoiceAndClientAndPerson::new),
                (Row3<Invoice, Client, Person> row) -> new InvoiceAndClientAndPerson(row.v1(), row.v2(), row.v3()),
                Invoice.class, Client.class, Person.class);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TestTemplate
    public void testSelectToRecordWithLambda(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.*,Person.* FROM Invoice,Client,Person WHERE Invoice.clientId=Client.id AND Client.id=Person.clientId",
                from(testDb, Invoice.class, Client.class, Person.class)
                        .where((i, c, p) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()))
                        .list((i, c, p) -> new InvoiceAndClientAndPerson(i, c, p)),
                (Row3<Invoice, Client, Person> row) -> new InvoiceAndClientAndPerson(row.v1(), row.v2(), row.v3()),
                Invoice.class, Client.class, Person.class);
    }

    @TestTemplate
    public void testSelectToListOfRow(TestDatabase testDb) throws SQLException {
        var testData = from(testDb, Invoice.class, Client.class, Person.class)
                .where((i, c, p) -> i.clientId().equals(c.id()) && c.id().equals(p.clientId()))
                .list();
        testListOfCustom(testDb, "SELECT Invoice.*,Client.*,Person.* FROM Invoice,Client,Person WHERE Invoice.clientId=Client.id AND Client.id=Person.clientId",
                testData,
                (Row3<Invoice, Client, Person> row) -> row,
                Invoice.class, Client.class, Person.class);
    }

    private record InvoiceAndClientAndPerson(Invoice invoice, Client client, Person person) {
    }


}
