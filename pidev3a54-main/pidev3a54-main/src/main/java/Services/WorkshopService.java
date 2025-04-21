package Services;

import Models.Workshops;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkshopService implements IService<Workshops> {
    private Connection con;

    public WorkshopService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Workshops workshop) throws SQLException {
        String query = "INSERT INTO workshops (titre, description, video) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, workshop.getTitre());
            stmt.setString(2, workshop.getDescription());
            stmt.setString(3, workshop.getVideo());

            int rows = stmt.executeUpdate();
            System.out.println("Workshop ajouté, lignes insérées : " + rows);

        }
    }

    @Override
    public void update(Workshops workshop) throws SQLException {
        String query = "UPDATE workshops SET titre = ?, description = ?, video = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setString(1, workshop.getTitre());
        stmt.setString(2, workshop.getDescription());
        stmt.setString(3, workshop.getVideo());
        stmt.setInt(4, workshop.getId());

        int rows = stmt.executeUpdate();
        System.out.println("Workshop modifié, lignes mises à jour : " + rows);
    }

    @Override
    public void delete(Workshops workshop) throws SQLException {
        String query = "DELETE FROM workshops WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, workshop.getId());

        int rows = stmt.executeUpdate();
        System.out.println("Workshop supprimé, lignes supprimées : " + rows);
    }

    @Override
    public List<Workshops> getAll() throws SQLException {
        String query = "SELECT * FROM workshops";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        List<Workshops> list = new ArrayList<>();
        while (rs.next()) {
            Workshops w = new Workshops();
            w.setId(rs.getInt("id"));
            w.setTitre(rs.getString("titre"));
            w.setDescription(rs.getString("description"));
            w.setVideo(rs.getString("video"));

            list.add(w);
        }

        return list;
    }

    public Workshops getById(int id) throws SQLException {
        String query = "SELECT * FROM workshops WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new Workshops(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getString("video")
            );
        }

        return null;
    }

//    public boolean isWorkshopTitleExists(String titre) throws SQLException {
//        String query = "SELECT COUNT(*) FROM workshops WHERE titre = ?";
//        PreparedStatement stmt = con.prepareStatement(query);
//        stmt.setString(1, titre);
//        ResultSet rs = stmt.executeQuery();
//
//        return rs.next() && rs.getInt(1) > 0;
//    }
}
