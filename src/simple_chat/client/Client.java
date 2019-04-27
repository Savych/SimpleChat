
package simple_chat.client;

import simple_chat.Connection;
import simple_chat.ConsoleHelper;
import simple_chat.Message;
import simple_chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Во время ожидания возникло исключение.");
                return;
            }
        }

        if(clientConnected)
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");

        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equals("exit"))
                break;
            else if (shouldSendTextFromConsole())
                sendTextMessage(text);
        }

    }

    protected String getServerAddress() {
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            Socket socket;
            try {
                socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e ) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST)
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else throw new IOException("Unexpected simple_chat.MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT)
                    processIncomingMessage(message.getData());
                else if (message.getType() == MessageType.USER_ADDED)
                    informAboutAddingNewUser(message.getData());
                else if (message.getType() == MessageType.USER_REMOVED)
                    informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected simple_chat.MessageType");
            }
        }
    }
}
