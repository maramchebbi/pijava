package Services;

import Models.Commentaire;
import Models.Oeuvre;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {
    Connection con;

    public CommentaireService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Commentaire commentaire) throws SQLException {
        String query = "INSERT INTO commentaire (contenu, date, user_id, oeuvre_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, commentaire.getContenu());
            stmt.setTimestamp(2, Timestamp.valueOf(commentaire.getDate()));
            stmt.setInt(3, commentaire.getUserId());

            if (commentaire.getOeuvre() != null) {
                stmt.setInt(4, commentaire.getOeuvre().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            int rows = stmt.executeUpdate();
            System.out.println("Commentaire ajouté, lignes affectées: " + rows);
        }
    }

    @Override
    public void update(Commentaire commentaire) throws SQLException {
        String query = "UPDATE commentaire SET contenu = ?, date = ?, user_id = ?, oeuvre_id = ? WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, commentaire.getContenu());
            stmt.setTimestamp(2, Timestamp.valueOf(commentaire.getDate()));
            stmt.setInt(3, commentaire.getUserId());

            if (commentaire.getOeuvre() != null) {
                stmt.setInt(4, commentaire.getOeuvre().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, commentaire.getId());

            int rows = stmt.executeUpdate();
            System.out.println("Commentaire mis à jour, lignes affectées: " + rows);
        }
    }

    @Override
    public void delete(Commentaire commentaire) throws SQLException {
        String query = "DELETE FROM commentaire WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, commentaire.getId());
            int rows = stmt.executeUpdate();
            System.out.println("Commentaire supprimé, lignes affectées: " + rows);
        }
    }

    @Override
    public List<Commentaire> getAll() throws SQLException {
        // Modifier cette ligne: utiliser "nom" au lieu de "username"
        String query = "SELECT c.*, u.nom FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "ORDER BY c.date DESC";

        List<Commentaire> commentaires = new ArrayList<>();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDate(rs.getTimestamp("date").toLocalDateTime());
                c.setUserId(rs.getInt("user_id"));

                // Création d'une oeuvre minimale avec juste l'ID
                Oeuvre oeuvre = new Oeuvre();
                oeuvre.setId(rs.getInt("oeuvre_id"));
                c.setOeuvre(oeuvre);

                commentaires.add(c);
            }
        }
        return commentaires;
    }

    // Méthode supplémentaire pour récupérer les commentaires par oeuvre
    public List<Commentaire> getByOeuvreId(int oeuvreId) throws SQLException {
        // Modifier cette ligne: utiliser "nom" au lieu de "username"
        String query = "SELECT c.*, u.nom FROM commentaire c " +
                "LEFT JOIN user u ON c.user_id = u.id " +
                "WHERE c.oeuvre_id = ? " +
                "ORDER BY c.date DESC";

        List<Commentaire> commentaires = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, oeuvreId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContenu(rs.getString("contenu"));
                    c.setDate(rs.getTimestamp("date").toLocalDateTime());
                    c.setUserId(rs.getInt("user_id"));

                    commentaires.add(c);
                }
            }
        }
        return commentaires;
    }


    public ResultSet getUserNameById(int userId) {
        try {
            String query = "SELECT nom FROM user WHERE id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, userId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour vérifier si un commentaire existe déjà
    public boolean commentaireExists(String contenu, int userId, int oeuvreId) throws SQLException {
        String query = "SELECT COUNT(*) FROM commentaire WHERE contenu = ? AND user_id = ? AND oeuvre_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, contenu);
            stmt.setInt(2, userId);
            stmt.setInt(3, oeuvreId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}