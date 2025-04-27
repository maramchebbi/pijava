package Services;

import Models.Peinture;
import Models.Style;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeintureService {
    private final Connection connection;
    private final StyleService styleService;

    public PeintureService() {
        connection = DataSource.getDataSource().getConnection();
        styleService = new StyleService();
    }

    public void add(Peinture peinture) throws SQLException {
        String query = "INSERT INTO peinture (titre, date_cr, tableau, type_id, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, peinture.getTitre());
            ps.setDate(2, Date.valueOf(peinture.getDateCr()));
            ps.setString(3, peinture.getTableau());
            ps.setInt(4, peinture.getStyle().getId());
            ps.setInt(5, peinture.getUserId());
            ps.executeUpdate();
        }
    }

    public void update(Peinture peinture) throws SQLException {
        String query = "UPDATE peinture SET titre = ?, date_cr = ?, tableau = ?, type_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, peinture.getTitre());
            ps.setDate(2, Date.valueOf(peinture.getDateCr()));
            ps.setString(3, peinture.getTableau());
            ps.setInt(4, peinture.getStyle().getId());
            ps.setInt(5, peinture.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM peinture WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {  // connection doit être initialisée
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Peinture supprimée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Si la suppression échoue, relancez l'exception
        }
    }


    public List<Peinture> getAll()throws SQLException {
        List<Peinture> peintures = new ArrayList<>();
        String query = "SELECT * FROM peinture";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Style style = styleService.getById(rs.getInt("type_id"));
                Peinture peinture = new Peinture(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getDate("date_cr").toLocalDate(),
                        rs.getString("tableau"),
                        style,
                        rs.getInt("user_id")
                );
                peintures.add(peinture);
            }
        }
        return peintures;
    }

    public Peinture getById(int id) throws SQLException {
        String query = "SELECT * FROM peinture WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Style style = styleService.getById(rs.getInt("type_id"));
                return new Peinture(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getDate("date_cr").toLocalDate(),
                        rs.getString("tableau"),
                        style,
                        rs.getInt("user_id")
                );
            }
        }
        return null;
    }

    public List<Peinture> getPeinturesParStyleNom(String styleNom) throws SQLException {
        List<Peinture> peintures = new ArrayList<>();

        Style style = styleService.getByType(styleNom);
        if (style == null) {
            System.out.println("Style introuvable : " + styleNom);
            return peintures; // liste vide
        }

        String query = "SELECT * FROM peinture WHERE type_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, style.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Peinture peinture = new Peinture(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getDate("date_cr").toLocalDate(),
                        rs.getString("tableau"),
                        style,
                        rs.getInt("user_id")
                );
                peintures.add(peinture);
            }
        }
        return peintures;
    }


    public String getUserEmailById(int userId) throws SQLException {
        String query = "SELECT email FROM user WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        return null;
    }

}
