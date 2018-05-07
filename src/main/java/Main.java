import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import moe.nikky.matterlink.api.ApiMessage;
import moe.nikky.matterlink.api.MessageHandler;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) throws IOException {

        MessageHandler handler = new MessageHandler();
        ConcurrentLinkedQueue<ApiMessage> queue = handler.getQueue();
        MessageHandler.Config config;

        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader("config.json"));
            config = gson.fromJson(reader, MessageHandler.Config.class);
            handler.setConfig(config);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            try (Writer writer = new FileWriter("config.json")) {
                gson.toJson(handler.getConfig(), writer);
            }
        }

        handler.setLogger((level, msg) -> System.out.printf("[%s] %s%n", level, msg));
        handler.start("Connecting..", true);

        new Thread(() -> {
            while (true) {
                ApiMessage next = queue.poll();
                if (next != null) {
                    System.out.println(next);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                handler.checkConnection();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Scanner sc = new Scanner(System.in);
        while (true) {
            String input = sc.nextLine();
            switch (input) {
                case "start": {
                    handler.start("start", false);
                    break;
                }
                case "stop": {
                    handler.stop("stop");
                    break;
                }
                default: {
                    handler.transmit(new ApiMessage().setText(input));
                }
            }

        }
    }
}
