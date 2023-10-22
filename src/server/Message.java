package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.*;

public class Message {
    private int id; // Идентификатор сообщения
    private int from; // ID отправителя
    private int to; // ID получателя
    private String msg; // Сообщение
    private Date date; // Дата отправки

    public Message(int from, int to, String msg) {
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getMsg() {
        return msg;
    }

    public Date getDate() {
        return date;
    }
    public static Message readMessage(User user) throws IOException, ParseException, SQLException {
        String request = user.getIs().readUTF(); // Ожидаем сообщение от пользователя
        JSONParser jsonParser = new JSONParser(); // Создаём парсер, так как сообщение от пользователя поступит в формате JSON
        JSONObject jsonObject = (JSONObject) jsonParser.parse(request); // Преварщаем сообщения пользователя в JSONObject
        int from = user.getId(); // Получаем ID отправителя
        if(jsonObject.containsKey("getPrivateMessageWith")){
            int toId = Integer.parseInt(jsonObject.get("getPrivateMessageWith").toString());
            sendHistoryChat(user, toId);
            return null;
        }else{
            String msg = jsonObject.get("message").toString(); // Тело сообщения (текст)
            int to = Integer.parseInt(jsonObject.get("to_id").toString()); // ID получателя сообщения
            Message message = new Message(from, to, msg); // Создаёи объект message
            return message; // Возвращаем объект сообщения тому, кто его ждёт
        }

    }
    public void save(){
        Connection connection = null; // Создаём объект подключения к БД
        try {
            connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
            Statement statement = connection.createStatement(); // Создаём заявление
            statement.executeUpdate("INSERT INTO messages (from_id, to_id, msg) VALUES ('"+this.from+"','"+this.to+"','"+this.msg+"')");
            statement.close();
        } catch (SQLException e) {
            System.out.println("Невозможно сохранить сообщение в базе данных");
        }
    }
    public static void sendHistoryChat(User user) throws SQLException, IOException {
        sendHistoryChat(user, 0);
    }
    public static void sendHistoryChat(User user, int toId) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
        Statement statement = connection.createStatement();
        ResultSet resultSet;
        if(toId == 0){
            resultSet = statement.executeQuery("select * from messages where to_id = 0");
        }else{
            resultSet = statement.executeQuery("select * from messages where from_id in("+toId+","+user.getId()+") and to_id in ("+toId+","+user.getId()+")");
        }
        while (resultSet.next()){
            Server.sendMessage(user, resultSet.getString("msg"));
        }
    }
}
