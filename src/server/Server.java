package server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            System.out.println("Сервер запущен");
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            while (true){
                Socket socket = serverSocket.accept(); // Ожидаем подключение клиента
                System.out.println("Клиент подключился");
                User user = new User(socket);
                users.add(user);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("message", "Введите команду /reg - для регистрации\n" +
                                    "/login - для авторизации");
                            user.getOut().writeUTF(jsonObject.toJSONString());
                            String command = Message.readMessage(user).getMsg();
                            while (true){
                                if(command.equals("/reg")){
                                    if(user.reg()) break;
                                }else if (command.equals("/login")){
                                    if(user.login()) break;
                                }else{
                                    sendMessage(user, "Неверная команда, попробуйте ещё раз");
                                }
                            }

                            sendMessage(user, user.getName()+" добро пожаловать на сервер");
                            sendOnlineUsers(users);
                            while (true) {
                                Message message = Message.readMessage(user);
                                String request = message.getMsg();
                                message.save();
                                System.out.println(user.getName()+": "+request);
                                for (User user1 : users) { // Перебираем подключенных пользователей
                                    if (user.equals(user1)) continue;
                                    sendMessage(user1, user.getName()+": "+request);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Потеряно соединение с клиентом");
                            users.remove(user);
                            try {
                                sendOnlineUsers(users);
                            }catch (IOException ex){
                                ex.printStackTrace();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    // user - кому отправляем, msg - сообщение которое отправляем
    public static void sendMessage(User user, String msg) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", msg);
        user.getOut().writeUTF(jsonObject.toJSONString());
    }

    public static void sendOnlineUsers(ArrayList<User> users) throws IOException {
        JSONArray jsonArray = new JSONArray(); // Список в формате JSON
        for (User user : users) { // Перебираем онлайн пользователей
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("name", user.getName());
            jsonUser.put("id", user.getId());
            jsonArray.add(jsonUser); // Добавялем имя пользователя в список
        }
        JSONObject jsonObject = new JSONObject(); // JSONObject для отправки данных на клиента в формате JSON
        jsonObject.put("users", jsonArray); // Добавляем список в объект JSON
        for (User user : users) { // Перебираем онлайн пользователей
            user.getOut().writeUTF(jsonObject.toJSONString()); // Отправляем список пользователей
        }
    }
}
