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
package razifx.java.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import razifx.core.data.AppInfo;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.control.Hyperlink;
import razifx.core.RaziLogger;

/**
 * AboutController.java
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class AboutController {

    @FXML
    private Label appVersionLabel;
    @FXML
    private Button move_main;
    @FXML
    private Hyperlink emailLink;
    @FXML
    private Hyperlink telegramLink;

    public void initialize() {

        // Set app version
        appVersionLabel.setText(AppInfo.getAppVersion()); 

    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/Main.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("حسابداری رازی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا", "Failed to load main application scene.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void openNewEmail(ActionEvent event) {
        String recipient = "mahdihoseinzade.jk@gmail.com";
        String subject = "RaziFX software application" + AppInfo.getAppVersion();
        String body = "Hello,\n";

        composeEmailInGmail(recipient, subject, body);
    }

    @FXML
    private void openTelegram(ActionEvent event) {
        String url = telegramLink.getText().toString();
        try {
            Desktop desktop = Desktop.getDesktop();
            if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
            } else {
                RaziLogger.warn("Desktop browsing is not supported on this platform.");
                try {
                    if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } else if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
                        Runtime.getRuntime().exec("open " + url);
                    } else { // Linux/Unix
                        Runtime.getRuntime().exec("xdg-open " + url); // or "gnome-open", "kfmclient openURL" etc.
                    }
                } catch (IOException ex) {
                    RaziLogger.error("Error opening browser using Runtime.exec: " + ex.getMessage());
                    showAlert("خطا هنگام بازکردن مرورگر", "Error opening browser using Runtime.exec", Alert.AlertType.NONE);
                    return;
                }
            }
        } catch (IOException | URISyntaxException e) {
            RaziLogger.warn(getClass().getName() + " : Error to opening browser -> " + e.getMessage());
            showAlert("خطا هنگام بازکردن مرورگر", "An error occured during opening browser.", Alert.AlertType.WARNING);
        }
    }

    public void composeEmailInGmail(String recipient, String subject, String body) {
        String gmailUrl = String.format("https://mail.google.com/mail/?view=cm&fs=1&to=%s&su=%s&body=%s",
                encodeValue(recipient), encodeValue(subject), encodeValue(body));

        openURLInDefaultBrowser(gmailUrl);
    }

    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8"); // Encode for URL
        } catch (java.io.UnsupportedEncodingException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return value; // Return original if encoding fails (less ideal)
        }
    }
    
    public void openURLInDefaultBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                RaziLogger.error("Error opening URL in default browser: " + e.getMessage());
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                showAlert("خطا در بازکردن مرورگر", "Error opening URL in default browser", Alert.AlertType.ERROR);
                return;
            }
        } else {
            showAlert("عدم پشتیبانی سیستم", "Desktop is not supported on this platform.", Alert.AlertType.ERROR);
            RaziLogger.warn("The user system does not support browser.");
        }
    }

}