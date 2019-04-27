package simple_chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не отправленно");
            }
        }
    }



    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt());
        System.out.println("Сервер включен");

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        }
        catch (IOException e){
            System.out.println(e + " произошла ошибка.");
            serverSocket.close();
        }
    }


    private static class Handler extends Thread {
        private Socket socket;

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Сoeдинение установлено с " + socket.getRemoteSocketAddress());
            String name = null;
            try {
                Connection connection = new Connection(socket);
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
            } catch (IOException | ClassNotFoundException e) {ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");}
            if (name != null) {
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            }
            ConsoleHelper.writeMessage("Соединение закрыто");
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT)
                    Server.sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                else ConsoleHelper.writeMessage("Ошибка! Это не текст");
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String nameFriend : connectionMap.keySet()) {
                if(!nameFriend.equals(userName))
                    connection.send(new Message(MessageType.USER_ADDED, nameFriend));
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "New Name"));
                Message answer = connection.receive();
                if (answer.getType() != MessageType.USER_NAME) continue;
                String name = answer.getData();
                if (name == null || name.isEmpty() || connectionMap.containsKey(name)) continue;
                connectionMap.put(name, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return name;
            }

        }

        public Handler(Socket socket) {
            this.socket = socket;
        }
    }
}
