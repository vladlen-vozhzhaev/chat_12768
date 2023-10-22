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
    private String name; // Имя пользователя (инкапсулированное поле доступ к нему только через геттер и сеттер)
    private Socket socket; // Сокет пользователя
    private DataInputStream is; // Поток ввода
    private DataOutputStream out; // Поток вывода
    private int id; // Идентификатор пользователя (хранится в БД)

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
        // this - это user у которого вызвали метод reg();
        Server.sendMessage(this, "Введите имя:"); // Отправляем сообщение пользователю
        String name = Message.readMessage(this).getMsg(); // Читаем ответ пользователя
        Server.sendMessage(this, "Введите логин:"); // Отправляем сообщение пользователю
        String login = Message.readMessage(this).getMsg(); // Читаем ответ пользователя
        Server.sendMessage(this, "Введите пароль:"); // Отправляем сообщение пользователю
        String pass = Message.readMessage(this).getMsg(); // Читаем ответ пользователя
        // Создаём объект подключения к базе данных
        Connection connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
        Statement statement = connection.createStatement(); // Создаём заявление
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"'"); // Отправка запроса к БД, ответ сохраним в ResultSet
        if (resultSet.next()){ // Если next() вернёт true, значит пользователь уже есть
            Server.sendMessage(this, "Такой пользователь уже зарегистрирован"); // Отправляем сообщение пользователю
            return false;
        }else{ // В другом случае (когда такого пользователя ещё нет)
            statement.executeUpdate("INSERT INTO users (name, login, pass) VALUES ('"+name+"', '"+login+"', '"+pass+"')"); // Добавляем пользователя в БД
            ResultSet reresultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"' AND pass='"+pass+"'"); // Достаём, только что добавленного пользователя
            reresultSet.next(); // Передвигаем курсор на строку в таблице
            this.setName(reresultSet.getString("name")); // Добавляем имя для user
            this.setId(reresultSet.getInt("id")); // Добавляем ID для user
            statement.close();
            return true;
        }
    }
    public boolean login() throws Exception{
        // this - это user у которого вызвали метод login();
        Server.sendMessage(this, "Введите логин:"); // Отправляем сообщение пользователю
        String login = Message.readMessage(this).getMsg();// Читаем ответ пользователя
        Server.sendMessage(this, "Введите пароль:"); // Отправляем сообщение пользователю
        String pass = Message.readMessage(this).getMsg();// Читаем ответ пользователя
        // Создаём объект подключения к базе данных
        Connection connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
        Statement statement = connection.createStatement(); // Создаём заявление
        ResultSet reresultSet = statement.executeQuery("SELECT * FROM users WHERE login = '"+login+"' AND pass='"+pass+"'"); // Выбираем пользователя по логин и паролю из БД
        if(reresultSet.next()){ // Если такой пользователь есть
            this.setName(reresultSet.getString("name"));// Добавляем имя для user
            this.setId(reresultSet.getInt("id"));// Добавляем ID для user
            return true; // Успешная авторизация
        }else{
            Server.sendMessage(this, "Неверный логин или пароль"); // Отправляем сообщение пользователю
            return false; // Авторизация не удалась
        }
    }
}
