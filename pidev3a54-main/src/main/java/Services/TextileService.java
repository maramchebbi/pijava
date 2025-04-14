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
        String query = "DELETE FROM textile WHERE id = '" + textile1.getId() + "'";
        Statement statement = con.createStatement();
        statement.executeUpdate(query);
    }


    public void deleteByCollectionId(int collectionId) throws SQLException {
        String query = "DELETE FROM textile WHERE collection_id = " + collectionId;
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
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
}
