package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestData;
import database.testing.TestDatabase;
import rec.Person;
import rec.Town;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.Random;

import static rec.Person.Sex.MALE;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0200_WithoutConstants extends DatabaseTest {

    private int getAge() {
        return 18;
    }

    @TestTemplate
    public void testWhereWithMethodCall(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>" + getAge(),
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > getAge())
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereWithArgument(TestDatabase testDb) throws SQLException {
        var age = 18;
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>" + getAge(),
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > age)
                        .list(), Person.class
        );

    }

    @TestTemplate
    public void testIntegerVarEq(TestDatabase testDb) throws SQLException {
        var age = Integer.valueOf(18);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=" + age, from(testDb, Person.class).where(person -> person.age() == age)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSelectWithChangingMethod(TestDatabase testDb) {
        var list1 = from(testDb, Town.class).where(t -> t.name().equals(randomTown().name()))
                .list();
        var name1 = list1.getFirst().name();
        var list2 = from(testDb, Town.class).where(t -> t.name().equals(randomTown().name()))
                .list();
        var name2 = list2.getFirst().name();
        assertThat(name1)
                .isNotEqualTo(name2);

    }

    @TestTemplate
    public void testSelectWithChangingMethodCached(TestDatabase testDb) {
        var query = from(testDb, Town.class).where(t -> t.name().equals(randomTown().name()));
        var name1 = query.list().getFirst().name();
        var name2 = query.list().getFirst().name();
        assertThat(name1)
                .isNotEqualTo(name2);
    }

    private String prev = "";

    private Town randomTown() {
        return randomTown(prev);
    }

    private Town randomTown(String doNotUse) {
        var town = TestData.towns.get(new Random().nextInt(TestData.towns.size()));
        if (town.name().equals(doNotUse)) {
            return randomTown(doNotUse);
        }
        prev = town.name();
        return town;
    }

}
