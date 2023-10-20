package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class User {
    private String name;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream out;
    private int id;

    public User(Socket socket) throws IOException {
        this.socket = socket;
        is = new DataInputStream(this.socket.getInputStream());
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public DataInputStream getIs() {
        return is;
    }

    public DataOutputStream getOut() {
        return out;
    }
    public int getId(){
        return id;
    }
    private void setId(int id){
        this.id = id;
    }
    public boolean reg() throws Exception {
        Server.sendMessage(this, "Введите имя:");
        String name = Message.readMessage(this).getMsg();
        Server.sendMessage(this, "Введите логин:");
        String login = Message.readMessage(this).getMsg();
        Server.sendMessage(this, "Введите пароль:");
        String pass = Message.readMessage(this).getMsg();
        Connection connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"'");
        if (resultSet.next()){
            Server.sendMessage(this, "Такой пользователь уже зарегистрирован");
            return false;
        }else{
            statement.executeUpdate("INSERT INTO users (name, login, pass) VALUES ('"+name+"', '"+login+"', '"+pass+"')");
            statement.close(); // При данной реализации SQL-инъекции
            ResultSet reresultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"' AND pass='"+pass+"'");
            reresultSet.next(); // Передвигаем курсор на строку в таблице
            this.setName(reresultSet.getString("name"));
            this.setId(reresultSet.getInt("id"));
            return true;
        }
    }
    public boolean login() throws Exception{
        Server.sendMessage(this, "Введите логин:");
        String login = Message.readMessage(this).getMsg();
        Server.sendMessage(this, "Введите пароль:");
        String pass = Message.readMessage(this).getMsg();
        Connection connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
        Statement statement = connection.createStatement();
        ResultSet reresultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"' AND pass='"+pass+"'");
        if(reresultSet.next()){
            this.setName(reresultSet.getString("name"));
            this.setId(reresultSet.getInt("id"));
            return true;
        }else{
            Server.sendMessage(this, "Неверный логин или пароль");
            return false;
        }
    }
}
