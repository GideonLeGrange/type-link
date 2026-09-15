import me.legrange.typelink.json.Json;
import me.legrange.typelink.sql.structure.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSerialization {

    // Test record classes
    record Person(Long id, String name, Integer age) {
    }

    record Invoice(Long id, Long personId, Double amount, Boolean paid) {
    }

    @Test
    public void toJson() {
        // Build a complete SqlQuery with all components
        // This represents: 
        // SELECT invoice.amount, invoice.paid 
        // FROM Person, Invoice
        // INNER JOIN Invoice ON person.id = invoice.personId
        // WHERE person.age > 18 AND invoice.paid = true
        // GROUP BY person.name
        // HAVING SUM(invoice.amount) > 1000
        // ORDER BY person.name ASC
        // LIMIT 10 OFFSET 5
        
        var query = new SqlQuery(
                // SELECT: invoice.amount, invoice.paid
                new SqlSelect(
                        new SqlTable(
                                new SqlTableRef(Invoice.class, "invoice"),
                                List.of(
                                        new SqlTableColumn("invoice", "amount", Double.class),
                                        new SqlTableColumn("invoice", "paid", Boolean.class)
                                )
                        ),
                        false
                ),
                // FROM: Person, Invoice
                new SqlFrom(List.of(new SqlTableRef(Person.class, "person"), new SqlTableRef(Invoice.class, "invoice"))),
                // JOIN: INNER JOIN Invoice ON person.id = invoice.personId
                new SqlJoin(List.of(
                        new SqlInnerJoin(
                                new SqlTableRef(Invoice.class, "invoice"),
                                new SqlEq(
                                        new SqlTableColumn("person", "id", Long.class),
                                        new SqlTableColumn("invoice", "personId", Long.class)
                                )
                        )
                )),
                // WHERE: person.age > 18 AND invoice.paid = true
                new SqlWhere(List.of(
                        new SqlAnd(
                                new SqlGt(
                                        new SqlTableColumn("person", "age", Integer.class),
                                        new SqlConstant(18)
                                ),
                                new SqlEq(
                                        new SqlTableColumn("invoice", "paid", Boolean.class),
                                        new SqlConstant(true)
                                )
                        )
                )),
                // GROUP BY: person.name
                new SqlGroupBy(List.of(
                        new SqlTableColumn("person", "name", String.class)
                )),
                // HAVING: SUM(invoice.amount) > 1000
                new SqlHaving(List.of(
                        new SqlGt(
                                new SqlSum(new SqlTableColumn("invoice", "amount", Double.class)),
                                new SqlConstant(1000.0)
                        )
                )),
                // ORDER BY: person.name ASC
                new SqlOrder(List.of(
                        new SqlColumnOrder(
                                new SqlTableColumn("person", "name", String.class),
                                false // false = ascending order
                        )
                )),
                // LIMIT: 10 OFFSET 5
                new SqlLimited(10, 5)
        );

        // Convert to JSON using the Json utility
        var json = Json.toJson(query);

        var deserialisedQuery = Json.fromJson(json);

        // Verify JSON is not empty and contains expected field names
        assertThat(query).isEqualTo(deserialisedQuery);

    }

}
