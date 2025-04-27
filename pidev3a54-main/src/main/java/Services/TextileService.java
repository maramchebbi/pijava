package Services;

import Models.collection_t;
import Models.textile;
import Utils.DataSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextileService implements IService<textile> {
    Connection con;
    private final Validator validator;

    public TextileService() {
        con = DataSource.getDataSource().getConnection();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    public void add(textile textile1) throws SQLException {
        // Validate the textile entity
        Set<ConstraintViolation<textile>> violations = validator.validate(textile1);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<textile> violation : violations) {
                errorMessage.append(violation.getMessage()).append("\n");
            }
            throw new SQLException(errorMessage.toString());
        }

        // Continue with DB insert if valid
        String query = "INSERT INTO textile (collection_id, nom, type, description, matiere, couleur, dimension, createur, image, technique, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setObject(1, textile1.getCollectionId());
        stmt.setString(2, textile1.getNom());
        stmt.setString(3, textile1.getType());
        stmt.setString(4, textile1.getDescription());
        stmt.setString(5, textile1.getMatiere());
        stmt.setString(6, textile1.getCouleur());
        stmt.setString(7, textile1.getDimension());
        stmt.setString(8, textile1.getCreateur());
        stmt.setString(9, textile1.getImage());
        stmt.setString(10, textile1.getTechnique());
        stmt.setInt(11, textile1.getUserId());

        int rows = stmt.executeUpdate();
        System.out.println("Rows inserted: " + rows);
    }

    @Override
    public void update(textile textile1) throws SQLException {
        String query = "UPDATE textile SET collection_id = ?, nom = ?, type = ?, description = ?, matiere = ?, couleur = ?, dimension = ?, createur = ?, image = ?, technique = ?, user_id = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setObject(1, textile1.getCollectionId()); // Use setObject to allow NULLs
        stmt.setString(2, textile1.getNom());
        stmt.setString(3, textile1.getType());
        stmt.setString(4, textile1.getDescription());
        stmt.setString(5, textile1.getMatiere());
        stmt.setString(6, textile1.getCouleur());
        stmt.setString(7, textile1.getDimension());
        stmt.setString(8, textile1.getCreateur());
        stmt.setString(9, textile1.getImage());
        stmt.setString(10, textile1.getTechnique());
        stmt.setInt(11, textile1.getUserId());
        stmt.setInt(12, textile1.getId());

        System.out.println("Updating textile with ID: " + textile1.getId());
        stmt.executeUpdate();
    }

    @Override
    public void delete(textile textile1) throws SQLException {
        // Supprimer d'abord les votes associés à ce textile
        deleteVotesByTextileId(textile1.getId());

        // Puis supprimer le textile
        String query = "DELETE FROM textile WHERE id = '" + textile1.getId() + "'";
        Statement statement = con.createStatement();
        statement.executeUpdate(query);
    }

    public void deleteVotesByTextileId(int textileId) throws SQLException {
        String query = "DELETE FROM vote WHERE textile_id = " + textileId;
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    public void deleteByCollectionId(int collectionId) throws SQLException {
        // Récupérer les IDs des textiles de cette collection
        List<Integer> textileIds = getTextileIdsByCollectionId(collectionId);

        // Supprimer les votes pour chaque textile
        for (Integer textileId : textileIds) {
            deleteVotesByTextileId(textileId);
        }

        // Puis supprimer les textiles
        String query = "DELETE FROM textile WHERE collection_id = " + collectionId;
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }

    private List<Integer> getTextileIdsByCollectionId(int collectionId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM textile WHERE collection_id = " + collectionId;

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }

        return ids;
    }

    @Override
    public List<textile> getAll() throws SQLException {
        String query = "SELECT * FROM textile";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<textile> textiles = new ArrayList<>();
        while (rs.next()) {
            textile t = new textile();
            t.setId(rs.getInt("id"));
            t.setCollectionId((Integer) rs.getObject("collection_id")); // Nullable
            t.setNom(rs.getString("nom"));
            t.setType(rs.getString("type"));
            t.setDescription(rs.getString("description"));
            t.setMatiere(rs.getString("matiere"));
            t.setCouleur(rs.getString("couleur"));
            t.setDimension(rs.getString("dimension"));
            t.setCreateur(rs.getString("createur"));
            t.setImage(rs.getString("image"));
            t.setTechnique(rs.getString("technique"));
            t.setUserId(rs.getInt("user_id"));
            textiles.add(t);
        }

        return textiles;
    }

    public List<collection_t> getAllCollections() throws SQLException {
        List<collection_t> collections = new ArrayList<>();

        String query = "SELECT * FROM collection_t";  // adjust table name if needed
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            collection_t col = new collection_t();
            col.setId(rs.getInt("id"));
            col.setNom(rs.getString("nom"));
            col.setUserId(rs.getInt("user_id")); // adjust the column name if it's different
            col.setDescription(rs.getString("description"));

            collections.add(col);
        }

        return collections;
    }

    public List<textile> getByCollectionId(int collectionId) throws SQLException {
        List<textile> textiles = new ArrayList<>();

        String query = "SELECT * FROM textile WHERE collection_id = " + collectionId;
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            textile t = new textile();
            t.setId(rs.getInt("id"));
            t.setCollectionId((Integer) rs.getObject("collection_id"));
            t.setNom(rs.getString("nom"));
            t.setType(rs.getString("type"));
            t.setDescription(rs.getString("description"));
            t.setMatiere(rs.getString("matiere"));
            t.setCouleur(rs.getString("couleur"));
            t.setDimension(rs.getString("dimension"));
            t.setCreateur(rs.getString("createur"));
            t.setImage(rs.getString("image"));
            t.setTechnique(rs.getString("technique"));
            t.setUserId(rs.getInt("user_id"));
            textiles.add(t);
        }

        return textiles;
    }

    public List<textile> searchTextiles(String searchTerm) throws SQLException {
        List<textile> textiles = new ArrayList<>();

        // Préparer la requête avec LIKE pour chercher dans plusieurs champs
        String query = "SELECT * FROM textile WHERE " +
                "nom LIKE ? OR " +
                "type LIKE ? OR " +
                "description LIKE ? OR " +
                "matiere LIKE ? OR " +
                "couleur LIKE ? OR " +
                "createur LIKE ? OR " +
                "technique LIKE ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";

            // Configurer tous les paramètres avec le même modèle de recherche
            for (int i = 1; i <= 7; i++) {
                stmt.setString(i, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    textile t = new textile();
                    t.setId(rs.getInt("id"));
                    t.setCollectionId((Integer) rs.getObject("collection_id"));
                    t.setNom(rs.getString("nom"));
                    t.setType(rs.getString("type"));
                    t.setDescription(rs.getString("description"));
                    t.setMatiere(rs.getString("matiere"));
                    t.setCouleur(rs.getString("couleur"));
                    t.setDimension(rs.getString("dimension"));
                    t.setCreateur(rs.getString("createur"));
                    t.setImage(rs.getString("image"));
                    t.setTechnique(rs.getString("technique"));
                    t.setUserId(rs.getInt("user_id"));
                    textiles.add(t);
                }
            }
        }

        return textiles;
    }


    public List<textile> getSortedTextiles(String sortCriteria, boolean ascending) throws SQLException {
        // Vérifier que le critère de tri est valide pour éviter les injections SQL
        String validCriteria = validateSortCriteria(sortCriteria);

        String query = "SELECT * FROM textile ORDER BY " + validCriteria +
                (ascending ? " ASC" : " DESC");

        List<textile> textiles = new ArrayList<>();
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                textile t = new textile();
                t.setId(rs.getInt("id"));
                t.setCollectionId((Integer) rs.getObject("collection_id"));
                t.setNom(rs.getString("nom"));
                t.setType(rs.getString("type"));
                t.setDescription(rs.getString("description"));
                t.setMatiere(rs.getString("matiere"));
                t.setCouleur(rs.getString("couleur"));
                t.setDimension(rs.getString("dimension"));
                t.setCreateur(rs.getString("createur"));
                t.setImage(rs.getString("image"));
                t.setTechnique(rs.getString("technique"));
                t.setUserId(rs.getInt("user_id"));
                textiles.add(t);
            }
        }

        return textiles;
    }


    private String validateSortCriteria(String criteria) {
        // Liste des critères de tri valides
        List<String> validCriteria = List.of(
                "id", "nom", "type", "matiere", "couleur",
                "dimension", "createur", "technique", "collection_id"
        );

        if (criteria != null && validCriteria.contains(criteria.toLowerCase())) {
            return criteria.toLowerCase();
        }

        // Critère par défaut si invalide
        return "nom";
    }


    public String getCollectionNameById(Integer collectionId) throws SQLException {
        if (collectionId == null) {
            return "Non spécifiée";
        }

        String query = "SELECT nom FROM collection_t WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, collectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        }

        return "Non spécifiée";
    }

    public textile getById(int id) throws SQLException {
        String query = "SELECT * FROM textile WHERE id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    textile t = new textile();
                    t.setId(rs.getInt("id"));
                    t.setCollectionId((Integer) rs.getObject("collection_id"));
                    t.setNom(rs.getString("nom"));
                    t.setType(rs.getString("type"));
                    t.setDescription(rs.getString("description"));
                    t.setMatiere(rs.getString("matiere"));
                    t.setCouleur(rs.getString("couleur"));
                    t.setDimension(rs.getString("dimension"));
                    t.setCreateur(rs.getString("createur"));
                    t.setImage(rs.getString("image"));
                    t.setTechnique(rs.getString("technique"));
                    t.setUserId(rs.getInt("user_id"));
                    return t;
                }
            }
        }

        return null;
    }
}