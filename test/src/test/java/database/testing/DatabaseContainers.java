package database.testing;

import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class DatabaseContainers {
    public static final MariaDBContainer<?> MARIADB = new MariaDBContainer("mariadb:10.5.5")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

}