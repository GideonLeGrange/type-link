package example;

import me.legrange.typelink.Database;

public class ExampleQuery1 {

    private int i = 2;

    /** This should cause an error */
    void example1(Database<Record> db) {
        var res = db.from(ExampleDto.class)
                .where(dto -> dto.id().equals(i << 4) )
                .list();
    }

    /** This should pass */
    void example2(Database<Record> db) {
        var res = db.from(ExampleDto.class)
                .where(dto -> dto.id() != 0 )
                .list();
    }

}
