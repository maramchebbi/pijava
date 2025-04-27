package Services;

import Models.collection_t;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionTService implements IService<collection_t> {
    Connection con;

    public CollectionTService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(collection_t collection) throws SQLException {
        String query = "INSERT INTO collection_t (nom, user_id, description) VALUES ('" +
                collection.getNom() + "', '" +
                collection.getUserId() + "', '" +
                collection.getDescription() + "')";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }

    @Override
    public void update(collection_t collection) throws SQLException {
        String query = "UPDATE collection_t SET nom = ?, user_id = ?, description = ? WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, collection.getNom());
        pstmt.setInt(2, collection.getUserId());
        pstmt.setString(3, collection.getDescription());
        pstmt.setInt(4, collection.getId());
        pstmt.executeUpdate();
    }
    @Override
    public void delete(collection_t collection) throws SQLException {
        // First, delete all textiles that reference this collection
        TextileService textileService = new TextileService(); // Create the textile service
        textileService.deleteByCollectionId(collection.getId());

        // Then, delete the collection
        String query = "DELETE FROM collection_t WHERE id = '" + collection.getId() + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
    @Override
    public List<collection_t> getAll() throws SQLException {
        String query = "SELECT * FROM collection_t";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<collection_t> collections = new ArrayList<>();
        while (rs.next()) {
            collection_t c = new collection_t();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setUserId(rs.getInt("user_id"));
            c.setDescription(rs.getString("description"));
            collections.add(c);
        }

        return collections;
    }

    public List<collection_t> searchCollections(String query) throws SQLException {
        String sql = "SELECT * FROM collection_t WHERE nom LIKE ? OR description LIKE ?";
        List<collection_t> collections = new ArrayList<>();

        try (
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    collection_t c = new collection_t();
                    c.setId(rs.getInt("id"));
                    c.setNom(rs.getString("nom"));
                    c.setDescription(rs.getString("description"));
                    c.setUserId(rs.getInt("user_id"));
                    collections.add(c);
                }
            }
        }
        return collections;
    }
}
