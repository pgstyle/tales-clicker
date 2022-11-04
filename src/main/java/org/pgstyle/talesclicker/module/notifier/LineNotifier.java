package org.pgstyle.talesclicker.module.notifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;
import org.pgstyle.talesclicker.application.Configuration;

/**
 * This class is the modular scheme implementation of a line message notifier,
 * it is the same as the original notification mechanism.
 *
 * @since 1.0
 * @author PGKan
 */
public final class LineNotifier implements Notifier {

    private String token = Configuration.getConfig().getModuleProperty("notifier", "line.token");

    @Override
    public boolean notifies(String payload) {
        Application.log(Level.INFO, "send notification to Line");
        payload = "{\"messages\":[{\"type\":\"text\",\"text\":\"" + payload + "\"}]}";
        Application.log(Level.DEBUG, payload);
        try {
            // prepare connection
            URL line = new URL("https://api.line.me/v2/bot/message/broadcast");
            HttpsURLConnection connection = (HttpsURLConnection) line.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/json;charset=utf-8;encoding=utf-8");
            connection.addRequestProperty("Authorization", "Bearer " + this.token);
            connection.setDoOutput(true);
            // send request
            connection.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
            // process response
            int code = connection.getResponseCode();
            switch (code / 100) {
            case 2: // success
                Application.log(Level.INFO, "notified: %d - %s", code, connection.getResponseMessage());
                return true;
            case 1:
            case 3: // http controls
                Application.log(Level.WARN, "warning: %d - %s", code, connection.getResponseMessage());
                return true;
            case 4:
            case 5: // http errors
                Application.log(Level.ERROR, "error: %d - %s", code, connection.getResponseMessage());
                InputStream is = connection.getErrorStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream(connection.getContentLength() + 4);
                int length = 0;
                byte[] buffer = new byte[1024];
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
                Application.log(Level.ERROR, os.toString(Optional.ofNullable(connection.getContentEncoding()).orElse("utf-8")));
                return false;
            default:
                Application.log(Level.WARN, "unknown: %d - %s", code, connection.getResponseMessage());
                return false;
            }
        } catch (IOException e) {
            Application.log(Level.ERROR, "failed to send notifies, %s", e);
            e.printStackTrace();
            return false;
        }
    }

}
