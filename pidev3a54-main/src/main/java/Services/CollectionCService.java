package Services;

import Models.collection_ceramic;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionCService implements IService<collection_ceramic> {
    Connection con;

    public CollectionCService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(collection_ceramic collection) throws SQLException {
        String query = "INSERT INTO ceramic_collection (user_id, nom_c, description_c) VALUES (?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, collection.getUser_id());
        pstmt.setString(2, collection.getNom_c());
        pstmt.setString(3, collection.getDescription_c());
        pstmt.executeUpdate();
    }

    @Override
    public void update(collection_ceramic collection) throws SQLException {
        String query = "UPDATE ceramic_collection SET user_id = ?, nom_c = ?, description_c = ? WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, collection.getUser_id());
        pstmt.setString(2, collection.getNom_c());
        pstmt.setString(3, collection.getDescription_c());
        pstmt.setInt(4, collection.getId());
        pstmt.executeUpdate();
    }

    @Override
    public void delete(collection_ceramic collection) throws SQLException {
        // If you want to delete related ceramic objects (like how textile was handled), do it here.

        String query = "DELETE FROM ceramic_collection WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, collection.getId());
        pstmt.executeUpdate();
    }

    @Override
    public List<collection_ceramic> getAll() throws SQLException {
        String query = "SELECT * FROM ceramic_collection";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<collection_ceramic> collections = new ArrayList<>();

        while (rs.next()) {
            collection_ceramic c = new collection_ceramic();
            c.setId(rs.getInt("id"));
            c.setUser_id(rs.getInt("user_id"));
            c.setNom_c(rs.getString("nom_c"));
            c.setDescription_c(rs.getString("description_c"));
            collections.add(c);
        }

        return collections;
    }
}
