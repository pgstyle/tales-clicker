package org.pgstyle.talesclicker.module.notifier;

import java.io.IOException;
import java.util.Objects;

import org.pgstyle.talesclicker.action.Action;
import org.pgstyle.talesclicker.application.Application;
import org.pgstyle.talesclicker.application.Application.Level;

public final class DisconnectDetector implements Detector {

    @Override
    public boolean detect() {
        return DisconnectCapture.fromImage(Action.getCapturer().capture()).isDisconnected();
    }

    @Override
    public String message() {
        String hostname = System.getenv("HOSTNAME");
        if (Objects.isNull(hostname)) {
            try {
                byte[] bytes = new byte[1024];
                int length = Runtime.getRuntime().exec("hostname").getInputStream().read(bytes);
                hostname = new String(bytes).substring(0, length);
            } catch (IOException e) {
                Application.log(Level.WARN, "failed to get hostname, %s", e);
                hostname = "localhost";
            }
        }
        return "{\"messages\":[{\"type\":\"text\",\"text\":\"" + hostname.trim() + " is down\"}]}";
    }
    
}
