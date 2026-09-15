package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import me.legrange.typelink.Row2;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings({"NewClassNamingConvention", "unchecked"})
public final class Test_2100_From2Simple extends DatabaseTest {
    private record InvoiceAndClient(Invoice invoice, Client client) {
    }

    private record ClientAndInvoice(Client client, Invoice invoice) {
    }

    @TestTemplate
    public void testSelectToRecordWithConstructorReference(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.* FROM Invoice,Client WHERE Invoice.clientId=Client.id",
                from(testDb, Invoice.class, Client.class)
                        .where((i, c) -> i.clientId().equals(c.id()))
                        .list(InvoiceAndClient::new),
                (Row2<Invoice, Client> row) -> new InvoiceAndClient(row.v1(), row.v2()),
                Invoice.class, Client.class);
    }

    @TestTemplate
    public void testSelectToOtherRecordWithConstructorReference(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.* FROM Invoice,Client WHERE Invoice.clientId=Client.id",
                from(testDb, Client.class, Invoice.class)
                        .where((c, i) -> i.clientId().equals(c.id()))
                        .list(ClientAndInvoice::new),
                (Row2<Invoice, Client> row) -> new ClientAndInvoice(row.v2(), row.v1()),
                Invoice.class, Client.class);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TestTemplate
    public void testSelectToRecordWithLambda(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.* FROM Invoice,Client WHERE Invoice.clientId=Client.id",
                from(testDb, Invoice.class, Client.class)
                        .where((i, c) -> i.clientId().equals(c.id()))
                        .list((i, c) -> new InvoiceAndClient(i, c)),
                (Row2<Invoice, Client> row) -> new InvoiceAndClient(row.v1(), row.v2()),
                Invoice.class, Client.class);
    }

    @TestTemplate
    public void testSelectToOtherWithLambda(TestDatabase testDb) throws SQLException {
        testListOfCustom(testDb, "SELECT Invoice.*,Client.* FROM Invoice,Client WHERE Invoice.clientId=Client.id",
                from(testDb, Invoice.class, Client.class)
                        .where((i, c) -> i.clientId().equals(c.id()))
                        .list((i, c) -> new ClientAndInvoice(c, i)),
                (Row2<Invoice, Client> row) -> new ClientAndInvoice(row.v2(), row.v1()),
                Invoice.class, Client.class);
    }

    @TestTemplate
    public void testSelectToListOfRow(TestDatabase testDb) throws SQLException {
        var testData = from(testDb, Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id()))
                .list();
        testListOfCustom(testDb, "SELECT Invoice.*,Client.* FROM Invoice,Client WHERE Invoice.clientId=Client.id",
                testData,
                (Row2<Invoice, Client> row) -> row,
                Invoice.class, Client.class);
    }


}
