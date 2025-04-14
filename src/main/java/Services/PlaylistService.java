package Services;

import Models.Music;
import Models.Playlist;
import Utils.DataSource;
import javafx.scene.control.ChoiceDialog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaylistService implements IService<Playlist> {
    private static Connection con;

    public PlaylistService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Playlist playlist) throws SQLException {
        String query = "INSERT INTO playlist (titre_p, user_id, description, date_creation) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, playlist.getTitre_p());
        pstmt.setInt(2,1);
        pstmt.setString(3, playlist.getDescription());
        pstmt.setDate(4, new java.sql.Date(playlist.getDate_creation().getTime()));
        pstmt.executeUpdate();
    }

    @Override
    public void update(Playlist playlist) throws SQLException {
        String query = "UPDATE playlist SET titre_p = ?, user_id = ?, description = ?, date_creation = ? WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, playlist.getTitre_p());
        pstmt.setInt(2, 1);
        pstmt.setString(3, playlist.getDescription());
        pstmt.setDate(4, new java.sql.Date(playlist.getDate_creation().getTime()));
        pstmt.setInt(5, playlist.getId());
        pstmt.executeUpdate();
    }

    @Override
    public void delete(Playlist playlist) throws SQLException {
        // First delete all entries in the join table
        String deleteJoin = "DELETE FROM musique_playlist WHERE playlist_id = ?";
        PreparedStatement pstmtJoin = con.prepareStatement(deleteJoin);
        pstmtJoin.setInt(1, playlist.getId());
        pstmtJoin.executeUpdate();

        // Then delete the playlist itself
        String deletePlaylist = "DELETE FROM playlist WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(deletePlaylist);
        pstmt.setInt(1, playlist.getId());
        pstmt.executeUpdate();
    }

    @Override
    public List<Playlist> getAll() throws SQLException {
        String query = "SELECT * FROM playlist";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<Playlist> playlistList = new ArrayList<>();

        while (rs.next()) {
            Playlist playlist = new Playlist();
            playlist.setId(rs.getInt("id"));
            playlist.setTitre_p(rs.getString("titre_p"));
            playlist.setUser_id(rs.getInt("user_id"));
            playlist.setDescription(rs.getString("description"));
            playlist.setDate_creation(rs.getDate("date_creation"));

            playlistList.add(playlist);
        }

        return playlistList;
    }

    public void addMusicToPlaylist(Music music, Playlist playlist) throws SQLException {
        String query = "INSERT INTO musique_playlist (musique_id, playlist_id) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, music.getId());
            preparedStatement.setInt(2, playlist.getId());
            preparedStatement.executeUpdate();
        }
    }

    public void removeMusicFromPlaylist(Music music, Playlist playlist) throws SQLException {
        String query = "DELETE FROM musique_playlist WHERE musique_id = ? AND playlist_id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, music.getId());
            preparedStatement.setInt(2, playlist.getId());
            preparedStatement.executeUpdate();
        }
    }





}
