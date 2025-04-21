package utils;

import models.User;
import service.UserService;

import java.io.*;

public class SessionPersistence {
    private static final String SESSION_FILE = "session.txt";

    public static void saveSession(String email) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            writer.write(email);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User loadSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String email = reader.readLine();
                if (email != null && !email.isEmpty()) {
                    UserService userService = new UserService();
                    return userService.getUserByEmail(email);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void clearSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
