package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.*;

public class Message {
    private int id; // Идентификатор сообщения
    private int from; // ID отправителя
    private int to; // ID получателя
    private String msg; // Сообщение
    private Date date; // Дата отправки
    private int type;

    public Message(int from, int to, String msg) {
        this.from = from;
        this.to = to;
        this.msg = msg;
        this.type = 1;
    }
    public Message(int from, int to, String msg, int type) {
        this.from = from;
        this.to = to;
        this.msg = msg;
        this.type = type;
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
        System.out.println(jsonObject.toJSONString());
        if(jsonObject.containsKey("getPrivateMessageWith")){
            int toId = Integer.parseInt(jsonObject.get("getPrivateMessageWith").toString());
            sendHistoryChat(user, toId);
            return null;
        } else if (jsonObject.containsKey("getFile")) {
            String fileName = jsonObject.get("getFile").toString();
            File file = new File("files/"+fileName);
            FileInputStream fis = new FileInputStream(file);
            user.getOut().writeUTF(String.valueOf(file.length()));
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int i;
            while ((i = bis.read(buffer)) != -1){
                user.getOut().write(buffer);
            }
            user.getOut().flush();
            file = null;
            return null;
        } else if (jsonObject.containsKey("fileName")) {
            // Принимаем файл
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long time = timestamp.getTime();
            String fileName = time+jsonObject.get("fileName").toString();
            long fileSize = Long.parseLong(jsonObject.get("fileSize").toString());
            int to = Integer.parseInt(jsonObject.get("to_id").toString()); // ID получателя сообщения
            FileOutputStream fos = new FileOutputStream("files/"+fileName, true);
            System.out.println("Принимаем файл "+fileName);
            byte[] buffer = new byte[1024];
            while (true){
                user.getIs().read(buffer);
                for(byte b : buffer){
                    fos.write(b);
                }
                fileSize -= 1024;
                if(fileSize <= 0) break;
            }
            Message message = new Message(user.getId(), to, fileName, 2);
            message.save();
            return null;
        } else{
            String msg = jsonObject.get("message").toString(); // Тело сообщения (текст)
            int to = Integer.parseInt(jsonObject.get("to_id").toString()); // ID получателя сообщения
            Message message = new Message(from, to, msg); // Создаёи объект message
            return message; // Возвращаем объект сообщения тому, кто его ждёт
        }

    }
    // user - кому отправляем, msg - сообщение которое отправляем
    public static void sendMessage(User user, String msg, boolean privateMessage, int from, int type) throws IOException {
        JSONObject jsonObject = new JSONObject(); // Создаём объект JSON и будем передавать данные на клиента всегда в этом формате
        jsonObject.put("message", msg); // Добавляем в объект JSON ключ "message" с некоторым сообщением
        jsonObject.put("from", from); // Передаём ID отправителя
        jsonObject.put("private", privateMessage);
        jsonObject.put("type", type);
        user.getOut().writeUTF(jsonObject.toJSONString()); // Передаём сообщение в формате JSON клиенту
    }
    public static void sendMessage(User user, String msg, int type) throws IOException {
        sendMessage(user, msg, false, 0, type);
    }
    public void save(){
        DataBase.update("INSERT INTO messages (from_id, to_id, msg, type) VALUES ('"+this.from+"','"+this.to+"','"+this.msg+"', '"+this.type+"')");
    }
    public static void sendHistoryChat(User user) throws SQLException, IOException {
        sendHistoryChat(user, 0);
    }
    public static void sendHistoryChat(User user, int toId) throws SQLException, IOException {
        boolean privateMessage = toId==0;
        String sql = toId == 0?"select from_id, name, msg, type from messages, users where to_id = 0 AND from_id = users.id":"SELECT from_id, name, msg, type FROM messages, users WHERE from_id in("+toId+","+user.getId()+") AND to_id in ("+toId+","+user.getId()+") AND users.id=from_id";
        ResultSet resultSet = DataBase.query(sql);
        while (resultSet.next())
            sendMessage(
                    user,
                    resultSet.getString("name")+": "+resultSet.getString("msg"),
                    !privateMessage,
                    resultSet.getInt("from_id"),
                    resultSet.getInt("type")
            );
    }
}
