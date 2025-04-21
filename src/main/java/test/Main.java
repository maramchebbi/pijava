package test;

import models.User;
import service.UserService;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        Connection connection=MyDataBase.getInstance().getConnection();
        Connection connection1=MyDataBase.getInstance().getConnection();

        System.out.println(connection);
        System.out.println(connection1);

        UserService UserService =new UserService();
        try {
         //   UserService.update(new User(1,"ben","ali",24));
            System.out.println(UserService.select());
        } catch (SQLException e) {
            System.out.println(e.getMessage());        }
    }

}
