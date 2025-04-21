package Services;

import Models.CeramicCollection;
import Models.Oeuvre;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OeuvreService implements IService<Oeuvre> {
    Connection con;

    public OeuvreService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Oeuvre oeuvre) throws SQLException {
        String query = "INSERT INTO oeuvre (nom, type, description, matiere, couleur, dimensions, image, categorie, user_id, ceramic_collection_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(query);

        // Set values from the oeuvre object
        stmt.setString(1, oeuvre.getNom());
        stmt.setString(2, oeuvre.getType());
        stmt.setString(3, oeuvre.getDescription());
        stmt.setString(4, oeuvre.getMatiere());
        stmt.setString(5, oeuvre.getCouleur());
        stmt.setString(6, oeuvre.getDimensions());
        stmt.setString(7, oeuvre.getImage());
        stmt.setString(8, oeuvre.getCategorie());
        stmt.setInt(9, oeuvre.getUser_id());

        // Check if collection is not null before accessing its ID
        if (oeuvre.getCollection() != null) {
            stmt.setInt(10, oeuvre.getCollection().getId());  // Set the collection ID from the linked CeramicCollection
        } else {
            // If there's no collection associated, set the value to null or handle it based on your logic
            stmt.setNull(10, java.sql.Types.INTEGER);  // You can adjust this depending on your use case
        }

        // Execute the update
        int rows = stmt.executeUpdate();

        System.out.println("Rows inserted: " + rows); // For debugging purposes
    }




    @Override

    public void update(Oeuvre oeuvre) throws SQLException {
        String query = "UPDATE oeuvre SET nom = ?, type = ?, description = ?, matiere = ?, couleur = ?, dimensions = ?, image = ?, categorie = ?, user_id = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setString(1, oeuvre.getNom());
        stmt.setString(2, oeuvre.getType());
        stmt.setString(3, oeuvre.getDescription());
        stmt.setString(4, oeuvre.getMatiere());
        stmt.setString(5, oeuvre.getCouleur());
        stmt.setString(6, oeuvre.getDimensions());
        stmt.setString(7, oeuvre.getImage());
        stmt.setString(8, oeuvre.getCategorie());
        stmt.setInt(9, oeuvre.getUser_id());

        stmt.setInt(10, oeuvre.getId());
        int rows = stmt.executeUpdate();
        System.out.println("Rows updated: " + rows);
    }

    @Override
    public void delete(Oeuvre oeuvre) throws SQLException {
        String query = "DELETE FROM oeuvre WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, oeuvre.getId());

        int rows = stmt.executeUpdate();
        System.out.println("Rows deleted: " + rows);
    }

    public void deleteByCollectionId(int collectionId) throws SQLException {
        String query = "DELETE FROM oeuvre WHERE ceramic_collection_id = '" + collectionId + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }


    @Override
    public List<Oeuvre> getAll() throws SQLException {
        String query = "SELECT * FROM oeuvre";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        List<Oeuvre> oeuvres = new ArrayList<>();
        while (rs.next()) {
            Oeuvre o = new Oeuvre();
            o.setId(rs.getInt("id"));
            o.setNom(rs.getString("nom"));
            o.setType(rs.getString("type"));
            o.setDescription(rs.getString("description"));
            o.setMatiere(rs.getString("matiere"));
            o.setCouleur(rs.getString("couleur"));
            o.setDimensions(rs.getString("dimensions"));
            o.setImage(rs.getString("image"));
            o.setCategorie(rs.getString("categorie"));
            o.setUser_id(rs.getInt("user_id"));
           // o.setCeramic_collection_id((Integer) rs.getObject("ceramic_collection_id")); // Nullable
            oeuvres.add(o);
        }

        return oeuvres;
        }


    public List<Oeuvre> getByCollectionId(int collectionId) throws SQLException {
        String query = "SELECT * FROM oeuvre WHERE ceramic_collection_id = ?";
        PreparedStatement statement = con.prepareStatement(query);
        statement.setInt(1, collectionId);
        ResultSet rs = statement.executeQuery();

        List<Oeuvre> oeuvres = new ArrayList<>();
        while (rs.next()) {
            Oeuvre o = new Oeuvre();
            o.setId(rs.getInt("id"));
            o.setNom(rs.getString("nom"));
            o.setImage(rs.getString("image"));


            // Créer et associer la collection
            CeramicCollection collection = new CeramicCollection();
            collection.setId(collectionId);
            o.setCollection(collection);

            oeuvres.add(o);
        }
        return oeuvres;
    }

    public boolean isOeuvreNameOrImageExists(String nom, String imagePath) throws SQLException {
        String query = "SELECT COUNT(*) FROM oeuvre WHERE nom = ? OR image = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, nom);
            stmt.setString(2, imagePath);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isOeuvreNameExists(String nom) throws SQLException {
        String query = "SELECT COUNT(*) FROM oeuvre WHERE nom = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, nom);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isOeuvreImageExists(String imagePath) throws SQLException {
        String query = "SELECT COUNT(*) FROM oeuvre WHERE image = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, imagePath);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}




