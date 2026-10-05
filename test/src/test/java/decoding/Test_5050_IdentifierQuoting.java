package decoding;

import me.legrange.typelink.IdentifierMode;
import me.legrange.typelink.sql.generator.IdentifierQuoter;
import me.legrange.typelink.sql.generator.SqlGenerator;
import org.junit.jupiter.api.Test;
import rec.Client;
import rec.Group;
import rec.Invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every table and column name is passed through an {@link IdentifierQuoter}, so reserved words such as
 * a {@code Group} table are valid SQL. The quote character depends on the database, so it comes from
 * the caller (normally {@code DatabaseMetaData.getIdentifierQuoteString()}).
 */
class Test_5050_IdentifierQuoting {

    private static String sql(CapturingDatabase db, IdentifierQuoter quoter) {
        return SqlGenerator.generate(db.captured(), quoter).sql();
    }

    @Test
    void quotesTablesAndColumnsWithBackticks() {
        var db = new CapturingDatabase();
        db.from(Group.class).where(g -> g.name().equals("x")).list();

        assertEquals("SELECT `Group`.`id`, `Group`.`name` FROM `Group` WHERE `Group`.`name` = ?", sql(db, IdentifierQuoter.of("`")));
    }

    @Test
    void quotesTablesAndColumnsWithDoubleQuotes() {
        var db = new CapturingDatabase();
        db.from(Group.class).where(g -> g.name().equals("x")).list();

        assertEquals("SELECT \"Group\".\"id\", \"Group\".\"name\" FROM \"Group\" WHERE \"Group\".\"name\" = ?", sql(db, IdentifierQuoter.of("\"")));
    }

    @Test
    void quotesJoinsAndOrdering() {
        var db = new CapturingDatabase();
        db.from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .orderBy((i, _) -> i.invoiceDate())
                .list((i, _) -> i);

        assertEquals("SELECT `Invoice`.`id`, `Invoice`.`clientId`, `Invoice`.`invoiceDate`, `Invoice`.`description`, `Invoice`.`amount`, `Invoice`.`paid` FROM `Invoice` INNER JOIN `Client` ON `Invoice`.`clientId` = `Client`.`id`"
                + " ORDER BY `Invoice`.`invoiceDate`", sql(db, IdentifierQuoter.of("`")));
    }

    @Test
    void noQuoterLeavesNamesAlone() {
        var db = new CapturingDatabase();
        db.from(Group.class).where(g -> g.name().equals("x")).list();

        assertEquals("SELECT Group.id, Group.name FROM Group WHERE Group.name = ?", SqlGenerator.generate(db.captured()).sql());
    }

    @Test
    void blankQuoteStringMeansQuotingIsUnsupported() {
        var db = new CapturingDatabase();
        db.from(Group.class).list();

        assertEquals("SELECT Group.id, Group.name FROM Group", sql(db, IdentifierQuoter.of(" ")));
    }

    @Test
    void aQuoteCharacterInsideAnIdentifierIsDoubled() {
        assertEquals("`odd``name`", IdentifierQuoter.of("`").quote("odd`name"));
    }

    // ---- AUTO: quote only what cannot be written bare ----

    @Test
    void autoQuotesOnlyReservedWords() {
        var db = new CapturingDatabase();
        db.from(Group.class).where(g -> g.name().equals("x")).list();

        assertEquals("SELECT `Group`.id, `Group`.name FROM `Group` WHERE `Group`.name = ?", sql(db, IdentifierQuoter.auto("`")));
    }

    @Test
    void autoLeavesPlainMixedCaseNamesAlone() {
        var db = new CapturingDatabase();
        db.from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .orderBy((i, _) -> i.invoiceDate())
                .list((i, _) -> i);

        assertEquals("SELECT Invoice.id, Invoice.clientId, Invoice.invoiceDate, Invoice.description, Invoice.amount, Invoice.paid FROM Invoice INNER JOIN Client ON Invoice.clientId = Client.id"
                + " ORDER BY Invoice.invoiceDate", sql(db, IdentifierQuoter.auto("\"")));
    }

    @Test
    void autoRecognisesReservedWordsInAnyCase() {
        assertEquals("\"order\"", IdentifierQuoter.auto("\"").quote("order"));
        assertEquals("\"Order\"", IdentifierQuoter.auto("\"").quote("Order"));
        assertEquals("\"GROUP\"", IdentifierQuoter.auto("\"").quote("GROUP"));
    }

    @Test
    void autoQuotesNamesThatAreNotPlainIdentifiers() {
        var auto = IdentifierQuoter.auto("`");
        assertEquals("`my table`", auto.quote("my table"));
        assertEquals("`2fast`", auto.quote("2fast"));
        assertEquals("`a-b`", auto.quote("a-b"));
        assertEquals("fine_name1", auto.quote("fine_name1"));
    }

    @Test
    void autoQuotesEachPartOfAQualifiedName() {
        var auto = IdentifierQuoter.auto("`");
        assertEquals("shop.`Group`", auto.quote("shop.Group"));
        assertEquals("`order`.items", auto.quote("order.items"));
        assertEquals("shop.`Group`", IdentifierQuoter.auto("`").quote("shop.Group"));
        assertEquals("`shop`.`Group`", IdentifierQuoter.of("`").quote("shop.Group"));
    }

    @Test
    void modesMapToQuoters() {
        assertEquals("Group", IdentifierQuoter.forMode(IdentifierMode.NEVER, "`").quote("Group"));
        assertEquals("`Group`", IdentifierQuoter.forMode(IdentifierMode.AUTO, "`").quote("Group"));
        assertEquals("`Person`", IdentifierQuoter.forMode(IdentifierMode.ALWAYS, "`").quote("Person"));
        assertEquals("Group", IdentifierQuoter.forMode(IdentifierMode.AUTO, " ").quote("Group"));
    }
}
