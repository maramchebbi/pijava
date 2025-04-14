package Services;

import Models.Sponsor;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService implements IService<Sponsor> {
    Connection con;

    public SponsorService() throws SQLException {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Sponsor sponsor) throws SQLException {
        if (sponsor.getNom() == null || sponsor.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du sponsor ne peut pas être vide.");
        }

        if (sponsor.getEmail() == null || sponsor.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email du sponsor ne peut pas être vide.");
        }

        if (sponsor.getTelephone() == null || sponsor.getTelephone().trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone du sponsor ne peut pas être vide.");
        }

        if (sponsor.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant du sponsor doit être positif.");
        }

        String query = "INSERT INTO sponsor (nom, type, email, telephone, site_web, logo, montant) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, sponsor.getNom());
        ps.setString(2, sponsor.getType());
        ps.setString(3, sponsor.getEmail());
        ps.setString(4, sponsor.getTelephone());
        ps.setString(5, sponsor.getSiteWeb());
        ps.setString(6, sponsor.getLogo());
        ps.setDouble(7, sponsor.getMontant());
        ps.executeUpdate();
    }

    @Override
    public void update(Sponsor sponsor) throws SQLException {
        // Validation manuelle
        if (sponsor.getNom() == null || sponsor.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du sponsor ne peut pas être vide.");
        }

        if (sponsor.getEmail() == null || sponsor.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email du sponsor ne peut pas être vide.");
        }

        if (sponsor.getTelephone() == null || sponsor.getTelephone().trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone du sponsor ne peut pas être vide.");
        }

        if (sponsor.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant du sponsor doit être positif.");
        }

        String query = "UPDATE sponsor SET nom = ?, type = ?, email = ?, telephone = ?, site_web = ?, logo = ?, montant = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, sponsor.getNom());
        stmt.setString(2, sponsor.getType());
        stmt.setString(3, sponsor.getEmail());
        stmt.setString(4, sponsor.getTelephone());
        stmt.setString(5, sponsor.getSiteWeb());
        stmt.setString(6, sponsor.getLogo());
        stmt.setDouble(7, sponsor.getMontant());
        stmt.setInt(8, sponsor.getId());
        stmt.executeUpdate();
    }

    @Override
    public void delete(Sponsor sponsor) throws SQLException {
        String query = "DELETE FROM sponsor WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, sponsor.getId());
        stmt.executeUpdate();
    }

    @Override
    public List<Sponsor> getAll() throws SQLException {
        String query = "SELECT * FROM sponsor";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<Sponsor> sponsors = new ArrayList<>();

        while (rs.next()) {
            Sponsor sponsor = new Sponsor();
            sponsor.setId(rs.getInt("id"));
            sponsor.setNom(rs.getString("nom"));
            sponsor.setType(rs.getString("type"));
            sponsor.setEmail(rs.getString("email"));
            sponsor.setTelephone(rs.getString("telephone"));
            sponsor.setSiteWeb(rs.getString("site_web"));
            sponsor.setLogo(rs.getString("logo"));
            sponsor.setMontant(rs.getDouble("montant"));
            sponsors.add(sponsor);
        }

        return sponsors;
    }
}
