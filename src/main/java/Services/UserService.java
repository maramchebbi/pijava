package Services;

import Models.User;
import Utils.DataSource;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    Connection connection;

    public UserService() {
System.out.println("UserService Constructor");
        connection = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, genre, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getNom());
        preparedStatement.setString(2, user.getPrenom());
        preparedStatement.setString(3, user.getGenre());
        preparedStatement.setString(4, user.getEmail());
        preparedStatement.setString(5, user.getPassword());
        preparedStatement.setString(6, user.getRole());

        preparedStatement.executeUpdate();
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET nom=?, prenom=?, genre=?, email=?, password=?, role=? WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getNom());
        preparedStatement.setString(2, user.getPrenom());
        preparedStatement.setString(3, user.getGenre());
        preparedStatement.setString(4, user.getEmail());
        preparedStatement.setString(5, user.getPassword());
        preparedStatement.setString(6, user.getRole());
        preparedStatement.setInt(7, user.getId());
        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(User user) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, user.getId());
        preparedStatement.executeUpdate();
    }


    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setNom(resultSet.getString("nom"));
            user.setPrenom(resultSet.getString("prenom"));
            user.setGenre(resultSet.getString("genre"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setRole(resultSet.getString("role"));

            users.add(user);
        }

        return users;
    }

    public User login(String email, String password) throws SQLException {
        System.out.println("ena f service");
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            String hashedPassword = rs.getString("password");
            if (org.mindrot.jbcrypt.BCrypt.checkpw(password, hashedPassword)) {

                     return new User(
                             rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("genre"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                );


            }
        }

        return null;
    }
    public boolean modifierMotDePasse(int userId, String ancien, String nouveau) throws SQLException {
        String sqlSelect = "SELECT password FROM user WHERE id = ?";
        PreparedStatement selectStmt = connection.prepareStatement(sqlSelect);
        selectStmt.setInt(1, userId);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            String hashed = rs.getString("password");
            if (BCrypt.checkpw(ancien, hashed)) {
                String nouveauHashed = BCrypt.hashpw(nouveau, BCrypt.gensalt());
                String sqlUpdate = "UPDATE user SET password = ? WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate);
                updateStmt.setString(1, nouveauHashed);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                return true;
            }
        }

        return false;
    }
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("genre"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
            );
        }
        return null;
    }
    public static boolean resetPassword(String token, String newPassword) {
        // Chercher l'utilisateur par token dans la base
        // Si trouvé, mettre à jour son mot de passe
        // UPDATE user SET password = ..., reset_token = NULL WHERE reset_token = token
        return true; // simulation
    }


}
