package service;

import models.Style;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StyleService {
    private final Connection connection;

    public StyleService() {
        connection = DataSource.getDataSource().getConnection();
    }

    public void add(Style style) throws SQLException {
        String query = "INSERT INTO style (type_p, description, extab) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, style.getType());
            ps.setString(2, style.getDescription());
            ps.setString(3, style.getExtab());
            ps.executeUpdate();
        }
    }

    public void update(Style style) throws SQLException {
        String query = "UPDATE style SET type_p = ?, description = ?, extab = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, style.getType());
            ps.setString(2, style.getDescription());
            ps.setString(3, style.getExtab());
            ps.setInt(4, style.getId());
            ps.executeUpdate();
        }
    }


    // Dans le service StyleService
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM style WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }




    public List<Style> getAll() throws SQLException {
        List<Style> styles = new ArrayList<>();
        String query = "SELECT * FROM style";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Style style = new Style(
                        rs.getInt("id"),
                        rs.getString("type_p"),
                        rs.getString("description"),
                        rs.getString("extab")
                );
                styles.add(style);
            }
        }
        return styles;
    }

    public Style getById(int id) throws SQLException {
        String query = "SELECT * FROM style WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Style(
                        rs.getInt("id"),
                        rs.getString("type_p"),
                        rs.getString("description"),
                        rs.getString("extab")
                );
            }
        }
        return null;
    }
    public Style getByType(String type) throws SQLException {
        String sql = "SELECT * FROM style WHERE type_p = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, type);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            String typeFromDB = resultSet.getString("type_p");
            String description = resultSet.getString("description");
            String extab = resultSet.getString("extab");

            return new Style(id, typeFromDB, description, extab);
        }
        return null;
    }


}
