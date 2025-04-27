package Services;

import Models.Rating;
import Utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RatingService {
    private final Connection connection;

    public RatingService() {
        connection = DataSource.getDataSource().getConnection();
    }

    public void saveRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO rating (peinture_id, note, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getPeintureId());
            stmt.setInt(2, rating.getNote());
            stmt.setInt(3, rating.getUser_id()); // Utilisation dynamique
            stmt.executeUpdate();
        }
    }

    public void saveOrUpdateRating(Rating rating) throws SQLException {
        Integer existingRating = getRatingForPeintureAndUser(rating.getPeintureId(), rating.getUser_id());
        if (existingRating == null) {
            // Insert
            saveRating(rating);
        } else {
            // Update
            String sql = "UPDATE rating SET note = ? WHERE peinture_id = ? AND user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, rating.getNote());
                stmt.setInt(2, rating.getPeintureId());
                stmt.setInt(3, rating.getUser_id());
                stmt.executeUpdate();
            }
        }
    }

    public Integer getRatingForPeintureAndUser(int peintureId, int userId) throws SQLException {
        String sql = "SELECT note FROM rating WHERE peinture_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, peintureId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("note");
                }
            }
        }
        return null;
    }
}
