package Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Music {
    int id;
    String titre;
    int artistId;
    String artistName;
    String genre;
    String description;
    Date dateSortie;
    String cheminFichier;
    String photo;
    private List<Playlist> playlists = new ArrayList<>(); // Many-to-many with Playlist

    public Music() {}

    public Music( String titre, int artistId, String artistName, String genre, String description, Date dateSortie,String cheminFichier, String photo) {
        this.titre = titre;
        this.artistId = artistId;
        this.artistName = artistName;
        this.genre = genre;
        this.description = description;
        this.dateSortie = dateSortie;
        this.cheminFichier = cheminFichier;
        this.photo = photo;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(Date dateSortie) {
        this.dateSortie = dateSortie;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", genre='" + genre + '\'' +
                ", description='" + description + '\'' +
                ", dateSortie=" + dateSortie +
                ", cheminFichier='" + cheminFichier + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }
}


