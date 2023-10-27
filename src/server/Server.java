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
        ArrayList<User> users = new ArrayList<>(); // Коллекция подключённых к чату пользователей
        try {
            ServerSocket serverSocket = new ServerSocket(9123); // Открываем порт для сервера
            System.out.println("Сервер запущен");
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            while (true){ // Бесконечный цикл, чтобы нащ сервер работал без перерыва и выходных
                Socket socket = serverSocket.accept(); // Ожидаем подключение клиента
                System.out.println("Клиент подключился");
                User user = new User(socket); // Создаём объект пользователя
                users.add(user); // Добавляем в коллекцию подключённых к чату пользователей
                Thread thread = new Thread(new Runnable() { // Открывем поток для общения с подключившимся пользователем
                    @Override
                    public void run() {
                        try {
                            Message.sendMessage(user, "Введите команду /reg - для регистрации\n" +
                                    "/login - для авторизации",1); // Отправляем jsonObject пользователю user
                            while (true){
                                String command = Message.readMessage(user).getMsg(); // Ожидаем сообщение от пользователя
                                if(command.equals("/reg")){
                                    if(user.reg()) break;
                                }else if (command.equals("/login")){
                                    if(user.login()) break;
                                }else{
                                    Message.sendMessage(user, "Неверная команда, попробуйте ещё раз",1);
                                }
                            }

                            Message.sendMessage(user, user.getName()+" добро пожаловать на сервер",1); // Отправляем сообщение пользователю
                            sendOnlineUsers(users); // Отправляем список онлайн пользователей
                            Message.sendHistoryChat(user);
                            while (true) {
                                Message message = Message.readMessage(user); // Ожидаем сообщение от пользователя
                                if(message == null) continue;
                                String request = message.getMsg(); // Получаем только текст сообщения
                                message.save(); // Сохраняем сообщение в базу данных
                                System.out.println(user.getName()+": "+request); // Печатаем на консоль сервера сообщения пользователя
                                for (User user1 : users) { // Перебираем подключенных пользователей
                                    if (user.getId() == user1.getId()) continue; //user.equals(user1)
                                    else if(message.getTo() == 0) // Отправляем сообщение очередному пользователю списка
                                        Message.sendMessage(user1, user.getName()+": "+request,1);
                                    else if (user1.getId() == message.getTo()) // Отпраляем сообщение в приватный чат (ЛС)
                                        Message.sendMessage(user1, user.getName()+": "+request, true, user.getId(),1);
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
                thread.start(); // Стартуем поток
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // В аргумент метода передаём список всех подключенных пользователей
    public static void sendOnlineUsers(ArrayList<User> users) throws IOException {
        JSONArray jsonArray = new JSONArray(); // Список в формате JSON jsonArray = []
        for (User user : users) { // Перебираем онлайн пользователей
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("name", user.getName());
            jsonUser.put("id", user.getId());
            // jsonUser = {"name": "ИМЯ ПОЛЬЗОВАТЕЛЯ", "id": 5}
            jsonArray.add(jsonUser); // Добавялем имя пользователя в список
        }
        // jsonArray = [{"id":1,  "name": "Ivan"},{"id":2, "name":"Igor"},{id:3, "name": "Oleg"}]
        JSONObject jsonObject = new JSONObject(); // JSONObject для отправки данных на клиента в формате JSON
        jsonObject.put("users", jsonArray); // Добавляем список в объект JSON
        // jsonObject = {"users": [{"id":1,  "name": "Ivan"},{"id":2, "name":"Igor"},{id:3, "name": "Oleg"}]}
        for (User user : users) { // Перебираем онлайн пользователей
            user.getOut().writeUTF(jsonObject.toJSONString()); // Отправляем список пользователей
        }
    }
}
