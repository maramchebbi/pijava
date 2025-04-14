package Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Playlist {
    int id;
    String titre_p;
    int user_id;
    String description;
    Date date_creation;
    private List<Music> musics = new ArrayList<>(); // Many-to-many with Music

    public Playlist() {}

    public Playlist(String titre_p, int user_id, String description, Date date_creation) {
        this.titre_p = titre_p;
        this.user_id = user_id;
        this.description = description;
        this.date_creation = date_creation;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre_p() {
        return titre_p;
    }

    public void setTitre_p(String titre_p) {
        this.titre_p = titre_p;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Date date_creation) {
        this.date_creation = date_creation;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", titre_p='" + titre_p + '\'' +
                ", user_id='" + user_id + '\'' +
                ", description='" + description + '\'' +
                ", date_creation=" + date_creation +
                '}';
    }
}
