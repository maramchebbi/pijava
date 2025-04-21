package service;

import models.Message;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatService {
    private final Connection connection;

    public ChatService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void sendMessage(User user, String content) throws SQLException {
        String sql = "INSERT INTO chat_messages (contenu, user_id, date_envoi) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, user.getId());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    public List<Message> getAllMessages() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT cm.contenu, cm.date_envoi, u.id, u.nom, u.email, u.role " +
                "FROM chat_messages cm JOIN user u ON cm.user_id = u.id " +
                "ORDER BY cm.date_envoi ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                messages.add(new Message(
                        rs.getString("contenu"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("date_envoi").toLocalDateTime()
                ));
            }
        }
        return messages;
    }}