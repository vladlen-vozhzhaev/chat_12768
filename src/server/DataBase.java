package server;

import java.sql.*;

// попробовать с удалённой БД
public class DataBase {
    public static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/chat_12768";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    private static Connection connection;
    private static Statement statement;

    private static Statement createStatement(){
        try {
            connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
            return connection.createStatement(); // Создаём заявление
        } catch (SQLException e) {
            System.out.println("Нет подключения к базе данных");
            throw new RuntimeException(e);
        }
    }
    public static void update(String sql){
        statement = createStatement();
        try {
            statement.executeUpdate(sql); // Добавляем пользователя в БД
        } catch (SQLException e) {
            System.out.println("Ошибка в SQL запросе");
            throw new RuntimeException(e);
        }
    }

    public static ResultSet query(String sql){
        statement = createStatement();
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("Ошибка в SQL запросе");
            throw new RuntimeException(e);
        }
        return resultSet;
    }
}
