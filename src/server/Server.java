package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(9123);
            System.out.println("Сервер запущен");
            while (true){
                Socket socket = serverSocket.accept(); // Ожидаем подключение клиента
                System.out.println("Клиент подключился");
                User user = new User(socket);
                users.add(user);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            user.getOut().writeUTF("Введите имя:");
                            String name = user.getIs().readUTF();
                            user.setName(name);
                            user.getOut().writeUTF(user.getName()+" добро пожаловать на сервер");
                            while (true) {
                                String request = user.getIs().readUTF();
                                System.out.println(user.getName()+": "+request);
                                for (User user1 : users) {
                                    if (user.equals(user1)) continue;
                                    user1.getOut().writeUTF(user.getName()+": "+request);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Потеряно соединение с клиентом");
                            users.remove(user);
                        }
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
