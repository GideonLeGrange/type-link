package database.testing;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import static database.testing.TestData.populateData;

public final class InMemoryDatabase implements TestDatabase {

    private static HikariDataSource dataSource;

    private HikariDataSource getDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10); // Max number of connections in the pool
        // Initialize the data source
        dataSource = new HikariDataSource(config);
        // Initialize the data source
        return new HikariDataSource(config);
    }

    private void start() {
        try {
            try (var con = getConnection(); var statement = con.createStatement()) {
                statement.execute("RUNSCRIPT FROM 'classpath:schema.sql'");
            }
            populateData(() -> {
                try {
                    return getConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InvocationTargetException | IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            dataSource = getDataSource();
            start();
        }
        return dataSource.getConnection();
    }

    @Override
    public void stop() {
        dataSource.close();
    }
}
