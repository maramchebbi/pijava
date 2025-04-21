package utils;

import models.User;
import java.io.*;

public class SessionStorage {
    private static final String FILE_PATH = "session.txt";

    public static void saveSession(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(user.getEmail());
            writer.newLine();
            writer.write(user.getPassword()); // attention si c’est hashé
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadSession() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String email = reader.readLine();
            String password = reader.readLine();
            return new String[]{email, password};
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearSession() {
        File file = new File(FILE_PATH);
        if (file.exists()) file.delete();
    }
}
