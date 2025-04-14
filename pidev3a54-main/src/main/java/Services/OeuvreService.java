package Services;

import Models.oeuvre;
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

public class OeuvreService implements IService<oeuvre> {
    Connection con;
    private final Validator validator;

    public OeuvreService() {
        con = DataSource.getDataSource().getConnection();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    public void add(oeuvre oeuvre1) throws SQLException {
        // Validate the oeuvre entity
        Set<ConstraintViolation<oeuvre>> violations = validator.validate(oeuvre1);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<oeuvre> violation : violations) {
                errorMessage.append(violation.getMessage()).append("\n");
            }
            throw new SQLException(errorMessage.toString());
        }

        // Continue with DB insert if valid
        String query = "INSERT INTO oeuvre (ceramic_collection_id, user_id, nom, type, description, matiere, couleur, dimensions, createur, image, categorie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setInt(1, oeuvre1.getCeramicCollectionId());
        stmt.setInt(2, oeuvre1.getUserId());
        stmt.setString(3, oeuvre1.getNom());
        stmt.setString(4, oeuvre1.getType());
        stmt.setString(5, oeuvre1.getDescription());
        stmt.setString(6, oeuvre1.getMatiere());
        stmt.setString(7, oeuvre1.getCouleur());
        stmt.setString(8, oeuvre1.getDimensions());
        stmt.setString(9, oeuvre1.getCreateur());
        stmt.setString(10, oeuvre1.getImage());
        stmt.setString(11, oeuvre1.getCategorie());

        int rows = stmt.executeUpdate();
        System.out.println("Rows inserted: " + rows);
    }

    @Override
    public void update(oeuvre oeuvre1) throws SQLException {
        String query = "UPDATE oeuvre SET ceramic_collection_id = ?, user_id = ?, nom = ?, type = ?, description = ?, matiere = ?, couleur = ?, dimensions = ?, createur = ?, image = ?, categorie = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setInt(1, oeuvre1.getCeramicCollectionId());
        stmt.setInt(2, oeuvre1.getUserId());
        stmt.setString(3, oeuvre1.getNom());
        stmt.setString(4, oeuvre1.getType());
        stmt.setString(5, oeuvre1.getDescription());
        stmt.setString(6, oeuvre1.getMatiere());
        stmt.setString(7, oeuvre1.getCouleur());
        stmt.setString(8, oeuvre1.getDimensions());
        stmt.setString(9, oeuvre1.getCreateur());
        stmt.setString(10, oeuvre1.getImage());
        stmt.setString(11, oeuvre1.getCategorie());
        stmt.setInt(12, oeuvre1.getId());

        System.out.println("Updating oeuvre with ID: " + oeuvre1.getId());
        stmt.executeUpdate();
    }

    @Override
    public void delete(oeuvre oeuvre1) throws SQLException {
        String query = "DELETE FROM oeuvre WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, oeuvre1.getId());
        stmt.executeUpdate();
    }

    @Override
    public List<oeuvre> getAll() throws SQLException {
        String query = "SELECT * FROM oeuvre";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<oeuvre> oeuvres = new ArrayList<>();
        while (rs.next()) {
            oeuvre o = new oeuvre();
            o.setId(rs.getInt("id"));
            o.setCeramicCollectionId(rs.getInt("ceramic_collection_id"));
            o.setUserId(rs.getInt("user_id"));
            o.setNom(rs.getString("nom"));
            o.setType(rs.getString("type"));
            o.setDescription(rs.getString("description"));
            o.setMatiere(rs.getString("matiere"));
            o.setCouleur(rs.getString("couleur"));
            o.setDimensions(rs.getString("dimensions"));
            o.setCreateur(rs.getString("createur"));
            o.setImage(rs.getString("image"));
            o.setCategorie(rs.getString("categorie"));
            oeuvres.add(o);
        }

        return oeuvres;
    }

    public List<Integer> getAllCollectionIds() throws SQLException {
        String query = "SELECT id FROM collection_t"; // Adjust the table name if necessary
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<Integer> collectionIds = new ArrayList<>();

        while (rs.next()) {
            collectionIds.add(rs.getInt("id")); // Add collection ID to the list
        }

        return collectionIds;
    }

}

