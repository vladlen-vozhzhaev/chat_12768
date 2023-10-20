package clinet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9123);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true){
                            out.writeUTF(scanner.nextLine());
                        }
                    }catch (IOException e){
                        System.out.println("Потеряно соединение с сервером");
                    }
                }
            });
            thread.start();
            JSONParser jsonParser = new JSONParser();
            while (true){
                String response = is.readUTF();
                try {
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
                    if (jsonObject.containsKey("users")){
                        JSONArray jsonArray = (JSONArray) jsonObject.get("users");
                        System.out.println("Список пользователей онлайн:");
                        for (int i = 0; i < jsonArray.size(); i++) {
                            String username = jsonArray.get(i).toString();
                            System.out.println(username);
                        }
                    }else{
                        String msg = jsonObject.get("message").toString();
                        System.out.println(msg);
                    }
                } catch (ParseException e) {
                    System.out.println("Неверный формат сообщения");
                }

            }
        } catch (IOException e) {
            System.out.println("Потеряно соединение с сервером");
        }
    }
}
