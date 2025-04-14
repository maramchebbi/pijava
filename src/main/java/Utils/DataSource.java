package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/pidev";
    private final String user = "root";
    private final String password = "";
    private static DataSource dataSource;

    private DataSource() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static  DataSource getDataSource() {
        if(dataSource == null)
            dataSource = new DataSource();
        return dataSource;
    }

    public Connection getConnection() {
        return connection;
    }
}
