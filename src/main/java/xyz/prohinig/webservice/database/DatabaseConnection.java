package xyz.prohinig.webservice.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost/test";

    public Connection getConnection() {
        Properties properties = new Properties();
        properties.setProperty("user", "postgres");
        properties.setProperty("password", "12345");

        try {
            return DriverManager.getConnection(DATABASE_URL, properties);
        } catch (SQLException exception) {
            return null;
        }
    }
}
