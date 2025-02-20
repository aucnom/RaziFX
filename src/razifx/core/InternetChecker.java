/*
 * The MIT License
 *
 * Copyright 2025 mahdihoseinzade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package razifx.core;

import javafx.application.Platform;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * InternetChecker.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class InternetChecker {

    private static final String TEST_URL = "https://www.aparat.com";
    private static final int CHECK_INTERVAL = 15; // Check every 15 seconds
    private static final int TIMEOUT = 3000; // Timeout for connection attempt (3 seconds)

    private ScheduledExecutorService executor;

    public void start() {
        RaziLogger.info("InternetChecker start.");
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::checkConnection, 0, CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    private void checkConnection() {
        try {
            URL url = new URL(TEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // do nothing
            } else {
                handleConnectionLoss("HTTP Error: " + responseCode);
            }
            connection.disconnect();

        } catch (IOException e) {
            handleConnectionLoss("Connection Error: " + e.getMessage());
        }
    }

    private void handleConnectionLoss(String reason) {
        RaziLogger.warn(reason);
        RaziLogger.info("The network connection lost.");

        // Important: Use Platform.runLater to exit on the JavaFX Application Thread
        Platform.runLater(() -> {
            showAlert("اتصال شما به شبکه قطع می باشد", "Please check your network conncetion or contact with your ISP.", Alert.AlertType.WARNING);
            System.exit(1);
        });

        // Optionally, stop the executor to prevent further checks
        executor.shutdownNow();
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow(); // Stop the background thread when your application closes
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
