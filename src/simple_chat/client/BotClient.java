package simple_chat.client;

import simple_chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%d", (int) (Math.random() * 100));
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(": ")) {
            String name = message.split(": ")[0];
            String text = message.split(": ")[1];
            String date = null;
            if(name != null && text != null) {
                switch (text) {
                    case "дата": {
                        SimpleDateFormat format = new SimpleDateFormat("d.MM.YYYY");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "день": {
                        SimpleDateFormat format = new SimpleDateFormat("d");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "месяц": {
                        SimpleDateFormat format = new SimpleDateFormat("MMMM");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "год": {
                        SimpleDateFormat format = new SimpleDateFormat("YYYY");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "время": {
                        SimpleDateFormat format = new SimpleDateFormat("H:mm:ss");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "час": {
                        SimpleDateFormat format = new SimpleDateFormat("H");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "минуты": {
                        SimpleDateFormat format = new SimpleDateFormat("m");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                    case "секунды": {
                        SimpleDateFormat format = new SimpleDateFormat("s");
                        date = format.format(Calendar.getInstance().getTime());
                        break;
                    }
                }
                if (date != null)
                    sendTextMessage(String.format("Информация для %s: %s", name, date));
            }
            }
        }
    }
}
