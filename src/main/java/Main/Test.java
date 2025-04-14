package Main;

import Models.Music;
import Services.MusicService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        MusicService musicService = new MusicService();

        try {
            // Ajouter un nouveau morceau de musique
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = sdf.parse("2023-12-20");

            Music newMusic = new Music();
            newMusic.setId(9);
            newMusic.setTitre("selim");
            newMusic.setArtistId(777);
            newMusic.setArtistName("Aurora Sky");
            newMusic.setGenre("Electronic");
            newMusic.setDescription("A futuristic ambient track.");
            newMusic.setDateSortie(releaseDate);
            newMusic.setCheminFichier("/music/dreamscape.mp3");
            newMusic.setPhoto("/images/dreamscape.jpg");

            // Ajout à la base de données
            //musicService.add(newMusic);
            musicService.delete(newMusic);
            System.out.println("Music added successfully!");

            // Affichage de tous les morceaux
            List<Music> musicList = musicService.getAll();
            for (Music m : musicList) {
                System.out.println("ID: " + m.getId() +
                        ", Titre: " + m.getTitre() +
                        ", Artist: " + m.getArtistName() +
                        ", Genre: " + m.getGenre());
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
