package Services;

import Models.CeramicCollection;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionCeramiqueService implements IService<CeramicCollection> {
    private Connection con;

    public CollectionCeramiqueService() {
        con = DataSource.getDataSource().getConnection();
    }

    // Ajouter une nouvelle collection
    @Override
    public void add(CeramicCollection collection) throws SQLException {
        String query = "INSERT INTO ceramic_collection (user_id, nom_c, description_c) VALUES (?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(query);

        // Définir les valeurs provenant de l'objet collection
        stmt.setInt(1, collection.getUser_id());
        stmt.setString(2, collection.getNom_c());
        stmt.setString(3, collection.getDescription_c());

        // Exécuter la mise à jour
        int rows = stmt.executeUpdate();
        System.out.println("Rows inserted: " + rows); // Pour le débogage
    }

    // Mettre à jour une collection
    @Override
    public void update(CeramicCollection collection) throws SQLException {
        String query = "UPDATE ceramic_collection SET user_id = ?, nom_c = ?, description_c = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setInt(1, collection.getUser_id());
        stmt.setString(2, collection.getNom_c());

        stmt.setString(3, collection.getDescription_c());

        stmt.setInt(4, collection.getId());
        int rows = stmt.executeUpdate();
        System.out.println("Rows updated: " + rows);
    }

    // Supprimer une collection

    @Override
    public void delete(CeramicCollection collection) throws SQLException {
        // Étape 1 : Supprimer les œuvres associées à cette collection
        OeuvreService oeuvreService = new OeuvreService(); // Créer le service des œuvres
        oeuvreService.deleteByCollectionId(collection.getId());

        // Étape 2 : Supprimer la collection
        String query = "DELETE FROM ceramic_collection WHERE id = '" + collection.getId() + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }



    // Récupérer toutes les collections
    @Override
    public List<CeramicCollection> getAll() throws SQLException {
        String query = "SELECT * FROM ceramic_collection";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        List<CeramicCollection> collections = new ArrayList<>();
        while (rs.next()) {
            CeramicCollection collection = new CeramicCollection();
            collection.setId(rs.getInt("id"));
            collection.setUser_id(rs.getInt("user_id"));
            collection.setNom_c(rs.getString("nom_c"));
            collection.setDescription_c(rs.getString("description_c"));

            collections.add(collection);
        }

        return collections;
    }

    // Récupérer une collection par son ID
    public CeramicCollection getById(int id) throws SQLException {
        String query = "SELECT * FROM ceramic_collection WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        CeramicCollection collection = null;
        if (rs.next()) {
            collection = new CeramicCollection();
            collection.setId(rs.getInt("id"));
            collection.setUser_id(rs.getInt("user_id"));
            collection.setNom_c(rs.getString("nom_c"));
            collection.setDescription_c(rs.getString("description_c"));

        }

        return collection;
    }

    public CeramicCollection getByName(String nom) throws SQLException {
        String query = "SELECT * FROM ceramic_collection WHERE nom_c = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, nom);
        ResultSet rs = stmt.executeQuery();

        CeramicCollection collection = null;
        if (rs.next()) {
            collection = new CeramicCollection();
            collection.setId(rs.getInt("id"));
            collection.setUser_id(rs.getInt("user_id")); // si pertinent
            collection.setNom_c(rs.getString("nom_c"));
            collection.setDescription_c(rs.getString("description_c"));
            // Ajoute d'autres setters si nécessaire
        }
        return collection;
    }


    //
    public boolean isNomCollectionExists(String nom) throws SQLException {
        String query = "SELECT COUNT(*) FROM ceramic_collection WHERE nom_c = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, nom);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0; // true si au moins une collection a ce nom
        }

        return false;
    }




}
