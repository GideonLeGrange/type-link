package database.testing;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.org.bouncycastle.util.io.Streams;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import static database.testing.TestData.populateData;

public final class TestContainerDatabase implements TestDatabase {

    public static TestContainerDatabase MARIADB = new TestContainerDatabase(DatabaseContainers.MARIADB);
    public static TestContainerDatabase POSTGRES = new TestContainerDatabase(DatabaseContainers.POSTGRES);

    private final JdbcDatabaseContainer<?> container;
    private HikariDataSource dataSource;

    public TestContainerDatabase(JdbcDatabaseContainer<?> container) {
        this.container = container;
    }

    private HikariDataSource getDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl() + "?useSSL=false&serverTimezone=UTC");
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setMaximumPoolSize(5); // Max number of connections in the pool
        // Initialize the data source
        dataSource = new HikariDataSource(config);
        // Initialize the data source
        return new HikariDataSource(config);
    }

    private void start() {
        try {
            try (var con = getConnection()) {
                var setup = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.sql");
                if (setup == null) {
                    throw new RuntimeException("Cannot find schema.sql");
                }
                var sql = new String(Streams.readAll(setup));
                executeScript(con, sql);
            }
            populateData(() -> {
                try {
                    return getConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InvocationTargetException | IllegalAccessException | SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            container.start();
            dataSource = getDataSource();
            start();
        }
        return dataSource.getConnection();
    }

    @Override
    public void stop() {
        dataSource.close();
    }

    private void executeScript(Connection con, String sql) throws SQLException {
        var lines = sql.split(";");
        try (var statement = con.createStatement()) {
            for (var line : lines) {
                if (!line.isBlank()) {
                    statement.execute(line);
                }
            }
        }
    }
}
