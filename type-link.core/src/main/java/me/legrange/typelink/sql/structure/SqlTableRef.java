package me.legrange.typelink.sql.structure;

/**
 * A table as it appears in a query, paired with the name it is known by in SQL.
 *
 * <p>The name is resolved through the {@link me.legrange.typelink.TableMapper} while parsing, where
 * the mapper is in hand, rather than being derived from the class when the SQL is generated. A
 * mapper is free to map a type to a table of another name, and {@code SqlTableColumn} has always
 * qualified columns with the mapped name; FROM and JOIN did not, and emitted the class's simple
 * name instead, so a renamed table produced SQL that referred to two different tables at once.
 *
 * @param type the class mapped to the table
 * @param name the table's name in SQL
 */
public record SqlTableRef(Class<?> type, String name) {
}
