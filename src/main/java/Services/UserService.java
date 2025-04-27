package Services;

import Models.User;
import Utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final Connection connection;

    public UserService() throws SQLException {
        this.connection = DataSource.getDataSource().getConnection();
    }

    // Ajouter un utilisateur
    public void add(User user) throws SQLException {
        String query = "INSERT INTO user (email, nom, prenom, genre, roles, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getGenre());
            ps.setString(5, String.join(",", user.getRoles())); // Convertit la liste en string
            ps.setString(6, user.getPassword());
            ps.executeUpdate();

            // Récupération de l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    // Mettre à jour un utilisateur
    public void update(User user) throws SQLException {
        String query = "UPDATE user SET email = ?, nom = ?, prenom = ?, genre = ?, roles = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getGenre());
            ps.setString(5, String.join(",", user.getRoles()));
            ps.setLong(6, user.getId());
            ps.executeUpdate();
        }
    }

    // Supprimer un utilisateur
    public void delete(long id) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // Récupérer tous les utilisateurs
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }

    // Récupérer un utilisateur par son ID
    public User getById(long id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Récupérer un utilisateur par email
    public User getByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Méthode utilitaire pour extraire un User d'un ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setGenre(rs.getString("genre"));

        // Conversion du string roles en List<String>
        String rolesStr = rs.getString("roles");
        List<String> roles = new ArrayList<>();
        if (rolesStr != null && !rolesStr.isEmpty()) {
            for (String role : rolesStr.split(",")) {
                roles.add(role.trim());
            }
        }
        user.setRoles(roles);

        user.setPassword(rs.getString("password"));
        return user;
    }

    // Changer le mot de passe
    public void changePassword(long userId, String newPassword) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    // Vérifier si un email existe déjà
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}