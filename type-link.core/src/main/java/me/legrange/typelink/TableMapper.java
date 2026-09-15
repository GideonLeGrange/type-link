package me.legrange.typelink;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Interface for mapping Java classes to SQL tables and their columns.
 *
 * <p>This interface provides methods to inspect and map between Java classes/methods/fields
 * and their corresponding SQL table and column representations. It supports reflection-based
 * introspection to determine which classes and members are mapped to SQL tables, retrieve
 * their SQL names, and assemble objects from query results.
 *
 * <p>Implementations of this interface handle the details of how Java classes are annotated
 * or configured to map to SQL artifacts, providing a bridge between object-oriented and
 * relational representations.
 */
public interface TableMapper<O> {

    /** Default mapper implementation that uses record-based mapping. */
    TableMapper<Record> RECORD_MAPPER = new RecordMapper<>(Record.class);


    /**
     * Determines whether the given class represents a SQL table.
     *
     * @param type the class to check
     * @return {@code true} if the class maps to a SQL table, {@code false} otherwise
     */
    boolean isTable(Class<? extends O> type);

    /**
     * Retrieves the SQL table name for the given class.
     *
     * @param type the class mapped to a SQL table
     * @return the SQL table name
     */
    String tableName(Class<? extends O> type);

    /**
     * Determines whether the given method represents a SQL column.
     *
     * @param method the method to check
     * @return {@code true} if the method maps to a SQL column, {@code false} otherwise
     */
    boolean isColumn(Method method);

    /**
     * Determines whether the given field represents a SQL column.
     *
     * @param field the field to check
     * @return {@code true} if the field maps to a SQL column, {@code false} otherwise
     */
    boolean isColumn(Field field);

    /**
     * Retrieves the SQL column name for the given method.
     *
     * @param method the method mapped to a SQL column
     * @return the SQL column name
     */
    String columnName(Method method);

    /**
     * Retrieves the SQL column name for the given field.
     *
     * @param field the field mapped to a SQL column
     * @return the SQL column name
     */
    String columnName(Field field);

    /**
     * Determines whether the given method represents a SQL column of {@code type}.
     *
     * <p>Prefer overriding this to {@link #isColumn(Method)} where one method can mean different
     * columns depending on which table is being read - see {@link #columnName(Class, Method)}.
     *
     * @param type   the class being read, as resolved from the query
     * @param method the method to check
     * @return {@code true} if the method maps to a SQL column of {@code type}
     */
    default boolean isColumn(Class<?> type, Method method) {
        return isColumn(method);
    }

    /** As {@link #isColumn(Class, Method)}, for a field read directly. */
    default boolean isColumn(Class<?> type, Field field) {
        return isColumn(field);
    }

    /**
     * Retrieves the SQL column name for the given method, on the given class.
     *
     * <p>{@link #tableName} already takes a type, because a table cannot be named without one. A
     * column usually can be, which is why {@link #columnName(Method)} exists and why this defaults
     * to it. It cannot be where a base class offers one accessor for a member each subtype names
     * for itself - a generic key reader, say, declared once and meaning a different column in every
     * table. Such a mapper overrides this, and answers from the type.
     *
     * @param type   the class being read, as resolved from the query
     * @param method the method mapped to a SQL column
     * @return the SQL column name within {@code type}
     */
    default String columnName(Class<?> type, Method method) {
        return columnName(method);
    }

    /** As {@link #columnName(Class, Method)}, for a field read directly. */
    default String columnName(Class<?> type, Field field) {
        return columnName(field);
    }

    /**
     * Retrieves all SQL column names for the given class.
     *
     * @param type the class mapped to a SQL table
     * @return a list of SQL column names defined for this class
     */
    List<String> columnNames(Class<? extends O> type);

    /**
     * Retrieves the Java type of a column in the given class.
     *
     * @param type the class mapped to a SQL table
     * @param name the SQL column name
     * @return the Java class representing the column's type
     */
    Class<?> columnType(Class<? extends O> type, String name);

    /**
     * Assembles an instance of the given class from a map of column values.
     *
     * <p>This method is typically used to construct objects from query result sets,
     * mapping column names to their corresponding values and populating the object.
     *
     * @param type the class to instantiate
     * @param values a map of column names to values from the query result
     * @return an instance of the class populated with the provided values
     */
    Object assemble(Class<? extends O> type, Map<String, Object> values);

}
