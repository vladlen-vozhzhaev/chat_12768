package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.*;

public class Message {
    private int id;
    private int from;
    private int to;
    private String msg;
    private Date date;

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
    public static Message readMessage(User user) throws IOException, ParseException {
        String request = user.getIs().readUTF();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(request);
        int from = user.getId();
        String msg = jsonObject.get("message").toString();
        int to = Integer.parseInt(jsonObject.get("to_id").toString());
        Message message = new Message(from, to, msg);
        return message;
    }
    public void save(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DataBase.DB_URL, DataBase.DB_USER, DataBase.DB_PASS);
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO messages (from_id, to_id, msg) VALUES ('"+this.from+"','"+this.to+"','"+this.msg+"')");
            statement.close();
        } catch (SQLException e) {
            System.out.println("Невозможно сохранить сообщение в базе данных");
        }

    }
}
