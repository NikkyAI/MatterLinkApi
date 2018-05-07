package matterlink.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
public class StreamConnection implements Runnable {
    private Thread thread;
    private final ConcurrentLinkedQueue<ApiMessage> rcvQueue;
    private HttpURLConnection urlConnection = null;
    private List<Consumer<Boolean>> onSuccessCallbacks = new LinkedList<>();
    private BiConsumer<String, String> logger;

    private String host = "";
    private String token = "";

    private boolean connected = false;
    private boolean connecting = false;
    private boolean cancelled = false;

    public StreamConnection(ConcurrentLinkedQueue<ApiMessage> rcvQueue) {
        this.rcvQueue = rcvQueue;
    }

    private Thread createThread() {
        Thread thread = new Thread(this);
        thread.setName("RcvThread");
        return thread;
    }

    public void addOnSuccess(Consumer<Boolean> callback) {
        onSuccessCallbacks.add(callback);
    }

    public void removeOnSuccess(Consumer<Boolean> callback) {
        onSuccessCallbacks.remove(callback);
    }

    private void onSuccess(boolean success) {
        connecting = false;
        connected = success;
        for (Consumer<Boolean> callback : onSuccessCallbacks) {
            callback.accept(success);
        }
    }

    @Override
    public void run() {
        try {
            String serviceURL = host + "/api/stream";
            URL myURL;

            myURL = new URL(serviceURL);
            urlConnection = (HttpURLConnection) myURL.openConnection();
            urlConnection.setRequestMethod("GET");
            if (!token.isEmpty()) {
                String bearerAuth = "Bearer " + token;
                urlConnection.setRequestProperty("Authorization", bearerAuth);
            }
            try (InputStream input = urlConnection.getInputStream()) {
                logger.accept("INFO", "connection opened");
                onSuccess(true);
//            BufferedInputStream bufferedInput = new BufferedInputStream(input, 8 * 1024);
                StringBuilder buffer = new StringBuilder();
                while (!cancelled) {
                    byte[] buf = new byte[1024];
                    Thread.sleep(10);
                    while (!(input.available() > 0)) {
                        if (cancelled) break;
                        Thread.sleep(10);
                    }
                    int chars = input.read(buf);

                    logger.accept("TRACE", String.format("read %d chars", chars));
                    if (chars > 0) {
                        String added = new String(Arrays.copyOfRange(buf, 0, chars));
                        logger.accept("DEBUG", "json: " + added);
                        buffer.append(added);
                        while (buffer.toString().contains("\n")) {
                            int index = buffer.indexOf("\n");
                            String line = buffer.substring(0, index);
                            buffer.delete(0, index + 1);
                            rcvQueue.add(ApiMessage.decode(line));
                        }
                    } else if (chars < 0) {
                        break;
                    }
                }
            } finally {
                onClose();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
            onSuccess(false);
        } catch (IOException e) {
            e.printStackTrace();
            onSuccess(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onClose() {
        logger.accept("INFO", "Bridge connection closed!");
        connected = false;
        connecting = false;
    }

    public void open() {
        if (thread == null || !thread.isAlive()) {
            thread = createThread();
            connecting = true;
            cancelled = false;
            thread.start();
            logger.accept("INFO", "Starting Connection");
        }
        if (thread.isAlive()) {
            logger.accept("INFO", "Bridge is connecting");
        }
    }

    public void close() {
        try {
            cancelled = true;
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            thread.join();
            thread = null;
            logger.accept("INFO", "Thread stopped");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setLogger(BiConsumer<String, String> logger) {
        this.logger = logger;
    }
}
