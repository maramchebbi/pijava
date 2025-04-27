package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/projetsymfony";
    private final String user = "root";
    private final String password = "";
    private static DataSource dataSource;

    private DataSource() {
        try {
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected!");
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static DataSource getDataSource() {
        if(dataSource == null)
            dataSource = new DataSource();
        return dataSource;
    }

    public Connection getConnection() {
        // Vérifier si la connexion est fermée et la rétablir si nécessaire
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection was closed, reconnecting...");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Reconnection successful!");
            }
        } catch (SQLException e) {
            System.out.println("Error checking/reconnecting: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}