package example;

import me.legrange.typelink.Database;

/**
 * This is an example of a query that will compile but fail at runtime. It is used to test the
 * ValidateQueries plugin. s
 */
public class ExampleQuery2 {

    private boolean isWhatWant(ExampleDto dto) {
        return dto.id() << 3 == 128;
    }
    /** This should cause an error */
    void example1(Database<Record> db) {
        var res = db.from(ExampleDto.class)
                .where(this::isWhatWant)
                .list();
    }

    /** This should pass */
    void example2(Database<Record> db) {
        var res = db.from(ExampleDto.class)
                .where(e -> isWhatWant(e))
                .list();
    }


}
