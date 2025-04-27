package service;

import models.Reclamation;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {
    private Connection connection;

    public ReclamationService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Reclamation reclamation) throws SQLException {
        String sql = "INSERT INTO reclamation (option, description, user_id, file_path) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, reclamation.getOption());
        stmt.setString(2, reclamation.getDescription());
        stmt.setInt(3, reclamation.getUser().getId());
        stmt.setString(4, reclamation.getFilePath()); // Ajout du chemin du fichier
        stmt.executeUpdate();
    }

    public List<Reclamation> getAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT r.*, u.nom, u.prenom, u.email FROM reclamation r JOIN user u ON r.user_id = u.id";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));

            Reclamation rec = new Reclamation(
                    rs.getInt("id"),
                    rs.getString("option"),
                    rs.getString("description"),
                    user,
                    rs.getString("file_path") // Récupération du chemin du fichier
            );
            list.add(rec);
        }

        return list;
    }

    public List<Reclamation> select() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT r.id, r.option, r.description, r.file_path, " +
                "u.id as user_id, u.nom, u.prenom, u.genre, u.email, " +
                "u.password, u.role, u.is_verified, u.verification_code " +
                "FROM reclamation r " +
                "JOIN user u ON r.user_id = u.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("genre"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationCode(rs.getString("verification_code"));

                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setOption(rs.getString("option"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setUser(user);
                reclamation.setFilePath(rs.getString("file_path"));

                list.add(reclamation);
            }
        }
        return list;
    }

    public void update(Reclamation reclamation) throws SQLException {
        String sql = "UPDATE reclamation SET option = ?, description = ?, file_path = ? WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, reclamation.getOption());
        stmt.setString(2, reclamation.getDescription());
        stmt.setString(3, reclamation.getFilePath());
        stmt.setInt(4, reclamation.getId());
        stmt.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
}