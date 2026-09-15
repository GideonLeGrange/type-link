package database.testing;

import java.sql.Connection;
import java.sql.SQLException;

public sealed interface TestDatabase permits InMemoryDatabase, TestContainerDatabase {

    Connection getConnection() throws SQLException;

    void stop();

}
