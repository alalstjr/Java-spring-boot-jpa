package me.whiteship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Application {

    public static void main(String[] args) throws SQLException {

        String url = "jdbc:postgresql://localhost:5432/springjpa";
        String username = "jjunpro";
        String password = "pass";

        try(Connection connection = DriverManager.getConnection(url, username, password))  {
            System.out.println("connection : " + connection);

            String sql = "CREATE TABLE ACCOUNT (id int, username varchar(255), password varchar(255));";
            try(PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.execute();
            }
        }

    }
}
