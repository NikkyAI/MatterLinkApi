package moe.nikky.matterlink.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
public class MessageHandler {
    private boolean enabled = false;

    private int connectErrors = 0;
    private int reconnectCooldown = 0;
    private int sendErrors = 0;
    private Config config;

    private ConcurrentLinkedQueue<ApiMessage> queue;
    private StreamConnection streamConnection;

    private BiConsumer<String, String> logger;

    {
        config = new Config();
        queue = new ConcurrentLinkedQueue<>();
        streamConnection = new StreamConnection(queue);

        streamConnection.addOnSuccess((success) -> {
            if (success) {
                logger.accept("INFO", "connected successfully");
                connectErrors = 0;
                reconnectCooldown = 0;
            } else {
                reconnectCooldown = connectErrors;
                connectErrors++;
                logger.accept("ERROR", String.format("connectErrors: %d", connectErrors));
            }
        });

        setLogger((level, msg) -> System.out.printf("[%s] %s%n", level, msg));
    }

    //TODO: make callbacks: onConnect onDisconnect onError etc

    public ConcurrentLinkedQueue<ApiMessage> getQueue() {
        return queue;
    }

    public void stop(String message) {
        if (message != null && config.announceDisconnect) {
            transmit(new ApiMessage().setText(message));
        }
        enabled = false;
        streamConnection.close();
    }


    public void start(String message, boolean clear) {
        config.sync(streamConnection);
        if (clear) {
            clear();
        }

        enabled = true;
        streamConnection.open();

        if (message != null && config.announceConnect) {
            transmit(new ApiMessage().setText(message));
        }
    }


    private void clear() {
        try {
            URL url = new URL(config.host + "/api/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (!config.token.isEmpty()) {
                String bearerAuth = "Bearer " + config.token;
                conn.setRequestProperty("Authorization", bearerAuth);
            }

            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {

                    logger.accept("TRACE", "skipping " + line);

                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transmit(ApiMessage msg) {
        if ((streamConnection.isConnected() || streamConnection.isConnecting())) {
            if (msg.getUsername().isEmpty())
                msg.setUsername(config.serverUser);
            if (msg.getGateway().isEmpty())
                msg.setGateway(config.gateway);
            logger.accept("INFO", "Transmitting: " + msg);
            transmitMessage(msg);
        }
    }

    private void transmitMessage(ApiMessage message) {
        try {
            URL url = new URL(config.host + "/api/message");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (!config.token.isEmpty()) {
                String bearerAuth = "Bearer " + config.token;
                conn.setRequestProperty("Authorization", bearerAuth);
            }

            String postData = message.encode();
            logger.accept("INFO", postData);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", "" + postData.getBytes().length);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData.getBytes());
            }

//            conn.getInputStream().close();
            conn.connect();
            int code = conn.getResponseCode();
            if (code != 200) {
                logger.accept("ERROR", "Server returned " + code);
                sendErrors++;
                if (sendErrors > 5) {
                    logger.accept("ERROR", "Caught too many errors, closing bridge");
                    stop("Interrupting Connection to matterbridge API due to status code " + code);
                }
            } else {
                sendErrors = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.accept("ERROR", "sending message caused " + e);
            sendErrors++;
            if (sendErrors > 5) {
                logger.accept("ERROR", "Caught too many errors, closing bridge");
                stop("Interrupting connection to matterbridge APIdue to accumulated errors sending messages");
            }
        }
    }


    private long nextCheck = 0;
    /**
     * clall this method every tick / cycle to make sure it is reconnecting
     */
    public void checkConnection() {
        if (enabled && !streamConnection.isConnected() && !streamConnection.isConnecting()) {
            logger.accept("TRACE", "check connection");
            logger.accept("TRACE", "next: " + nextCheck);
            logger.accept("TRACE", "now: " + System.currentTimeMillis());
            if(nextCheck > System.currentTimeMillis()) return;
            nextCheck = System.currentTimeMillis() + config.reconnectWait;

            if (connectErrors >= 10) {
                logger.accept("ERROR", "Caught too many errors, closing bridge");
                stop("Interrupting connection to matterbridge API due to accumulated connection errors");
                return;
            }

            if (reconnectCooldown <= 0) {
                logger.accept("INFO", "Trying to reconnect");
                start("Reconnecting to matterbridge API after connection error", false);
            } else {
                reconnectCooldown--;
            }
        }
    }

    public void setLogger(BiConsumer<String, String> logger) {
        streamConnection.setLogger(logger);
        this.logger = logger;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
        config.sync(streamConnection);
    }

    public class Config {
        public String host = "";
        public String token = "";
        public boolean announceConnect = true;
        public boolean announceDisconnect = true;
        public long reconnectWait = 500;
        public String gateway = "matterlink";
        public String serverUser = "Server";

        private void sync(StreamConnection connection) {
            connection.setToken(token);
            connection.setHost(host);
        }
    }
}
