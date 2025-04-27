package Services;

import Models.Vote;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteService implements IService<Vote> {
    private Connection con;

    public VoteService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Vote vote) throws SQLException {
        String query = "INSERT INTO vote (textile_id, user_id, value) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, vote.getTextileId());
            stmt.setInt(2, vote.getUserId());
            stmt.setInt(3, vote.getValue());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    vote.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Vote vote) throws SQLException {
        String query = "UPDATE vote SET value = ? WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, vote.getValue());
            stmt.setInt(2, vote.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Vote vote) throws SQLException {
        String query = "DELETE FROM vote WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, vote.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Vote> getAll() throws SQLException {
        List<Vote> votes = new ArrayList<>();
        String query = "SELECT * FROM vote";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Vote vote = new Vote();
                vote.setId(rs.getInt("id"));
                vote.setTextileId(rs.getInt("textile_id"));
                vote.setUserId(rs.getInt("user_id"));
                vote.setValue(rs.getInt("value"));
                votes.add(vote);
            }
        }

        return votes;
    }

    /**
     * Vérifie si un utilisateur a déjà voté pour un textile
     * @param textileId ID du textile
     * @param userId ID de l'utilisateur
     * @return Vote si trouvé, null sinon
     */
    public Vote getUserVote(int textileId, int userId) throws SQLException {
        String query = "SELECT * FROM vote WHERE textile_id = ? AND user_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, textileId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vote vote = new Vote();
                    vote.setId(rs.getInt("id"));
                    vote.setTextileId(rs.getInt("textile_id"));
                    vote.setUserId(rs.getInt("user_id"));
                    vote.setValue(rs.getInt("value"));
                    return vote;
                }
            }
        }

        return null;
    }

    /**
     * Calcule la note moyenne pour un textile
     * @param textileId ID du textile
     * @return Note moyenne ou 0 si aucun vote
     */
    public double getAverageRating(int textileId) throws SQLException {
        String query = "SELECT AVG(value) as avg_rating FROM vote WHERE textile_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, textileId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        }

        return 0;
    }

    /**
     * Obtient le nombre total de votes pour un textile
     * @param textileId ID du textile
     * @return Nombre de votes
     */
    public int getVoteCount(int textileId) throws SQLException {
        String query = "SELECT COUNT(*) as vote_count FROM vote WHERE textile_id = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, textileId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("vote_count");
                }
            }
        }

        return 0;
    }

    /**
     * Ajoute ou met à jour un vote
     * @param textileId ID du textile
     * @param userId ID de l'utilisateur
     * @param value Valeur du vote (1-5)
     * @return true si succès, false sinon
     */
    public boolean addOrUpdateVote(int textileId, int userId, int value) throws SQLException {
        Vote existingVote = getUserVote(textileId, userId);

        if (existingVote != null) {
            // Mettre à jour le vote existant
            existingVote.setValue(value);
            update(existingVote);
        } else {
            // Ajouter un nouveau vote
            Vote newVote = new Vote(textileId, userId, value);
            add(newVote);
        }

        return true;
    }
}