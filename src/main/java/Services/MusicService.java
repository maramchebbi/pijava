package Services;

import Models.Music;
import Models.Playlist;
import Utils.DataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicService implements IService<Music> {
    Connection con;

    public MusicService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Music music) throws SQLException {
        String query = "INSERT INTO musique (titre, artist_id, artist_name, genre, description, date_sortie, chemin_fichier, photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, music.getTitre());
        pstmt.setInt(2, 1);
        pstmt.setString(3, music.getArtistName());
        pstmt.setString(4, music.getGenre());
        pstmt.setString(5, music.getDescription());
        pstmt.setDate(6, new java.sql.Date(music.getDateSortie().getTime()));
        pstmt.setString(7, music.getCheminFichier());
        pstmt.setString(8, music.getPhoto());
        pstmt.executeUpdate();
    }

    @Override
    public void update(Music music) throws SQLException {
        String query = "UPDATE musique SET titre = ?, artist_id = ?, artist_name = ?, genre = ?, description = ?, date_sortie = ?, chemin_fichier = ?, photo = ? WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, music.getTitre());
        pstmt.setInt(2, 1);
        pstmt.setString(3, music.getArtistName());
        pstmt.setString(4, music.getGenre());
        pstmt.setString(5, music.getDescription());
        pstmt.setDate(6, new java.sql.Date(music.getDateSortie().getTime()));
        pstmt.setString(7, music.getCheminFichier());
        pstmt.setString(8, music.getPhoto());
        pstmt.setInt(9, music.getId());
        pstmt.executeUpdate();
    }

    @Override
    public void delete(Music music) throws SQLException {
        // Step 1: Delete from the join table (playlist_music)
        String deleteFromJoinTable = "DELETE FROM musique_playlist WHERE musique_id = ?";
        PreparedStatement pstmt1 = con.prepareStatement(deleteFromJoinTable);
        pstmt1.setInt(1, music.getId());
        pstmt1.executeUpdate();

        // Step 2: Delete from musique table
        String deleteFromMusic = "DELETE FROM musique WHERE id = ?";
        PreparedStatement pstmt2 = con.prepareStatement(deleteFromMusic);
        pstmt2.setInt(1, music.getId());
        pstmt2.executeUpdate();
    }


    @Override
    public List<Music> getAll() throws SQLException {
        String query = "SELECT * FROM musique";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<Music> musicList = new ArrayList<>();

        while (rs.next()) {
            Music music = new Music();
            music.setId(rs.getInt("id"));
            music.setTitre(rs.getString("titre"));
            music.setArtistId(rs.getInt("artist_id"));
            music.setArtistName(rs.getString("artist_name"));
            music.setGenre(rs.getString("genre"));
            music.setDescription(rs.getString("description"));
            music.setDateSortie(rs.getDate("date_sortie"));
            music.setCheminFichier(rs.getString("chemin_fichier"));
            music.setPhoto(rs.getString("photo"));

            musicList.add(music);
        }

        return musicList;
    }

    public List<Music> getMusicsByPlaylistId(int playlistId) throws SQLException {
        List<Music> musics = new ArrayList<>();
        String query = "SELECT m.* FROM musique m " +
                "JOIN musique_playlist pm ON m.id = pm.musique_id " +
                "WHERE pm.playlist_id = ?";

        PreparedStatement pst = con.prepareStatement(query);
        pst.setInt(1, playlistId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Music music = new Music();
            music.setId(rs.getInt("id"));
            music.setTitre(rs.getString("titre"));
            music.setArtistId(rs.getInt("artist_id"));
            music.setArtistName(rs.getString("artist_name"));
            music.setGenre(rs.getString("genre"));
            music.setDescription(rs.getString("description"));
            music.setDateSortie(rs.getDate("date_sortie"));
            music.setCheminFichier(rs.getString("chemin_fichier"));
            music.setPhoto(rs.getString("photo"));

            musics.add(music);
        }

        return musics;
    }

    public List<String> getAllGenres() throws SQLException {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM musique";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String genre = rs.getString("genre");
                if (genre != null && !genre.isEmpty()) {
                    genres.add(genre);
                }
            }
        }
        return genres;
    }

    public List<Music> getByGenre(String genre) throws SQLException {
        List<Music> musics = new ArrayList<>();
        String sql = "SELECT * FROM musique WHERE genre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, genre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Music music = new Music();
                music.setId(rs.getInt("id"));
                music.setTitre(rs.getString("titre"));
                music.setArtistId(rs.getInt("artist_id"));
                music.setArtistName(rs.getString("artist_Name"));
                music.setGenre(rs.getString("genre"));
                music.setDescription(rs.getString("description"));
                music.setDateSortie(rs.getDate("date_sortie"));
                music.setCheminFichier(rs.getString("chemin_fichier"));
                music.setPhoto(rs.getString("photo"));
                musics.add(music);
            }
        }

        return musics;
    }

}

