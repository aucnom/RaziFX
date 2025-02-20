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
package razifx;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import razifx.core.InternetChecker;
import razifx.core.RaziLogger;
import razifx.core.preloader.RaziPreloader;

/**
 * Main.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 * @version 1.0.12
 */
public class Main extends Application {

    private static final int COUNT_LIMIT = 99999;

    /**
     * entry point for application
     * 
     * @param primaryStage This is the main window of your application.
     * @throws Exception 
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // If SuperUser doesn't exist, load the registration scene
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/login/users.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("ورود");
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest((t) -> {
            t.consume();
            logout(primaryStage);
        });

    }

    /**
     * <h4>Preloader.ProgressNotification(progress)</h4>
     * <p>
     * Update the progress bar based on the received progress</p>
     *
     * @see razifx.core.preloader.RaziPreloader
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        RaziLogger.info("Application start: Main class loaded.");
        for (int i = 0; i < COUNT_LIMIT; i++) {
            double progress = (100 * i) / COUNT_LIMIT;
            LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(progress));
        }
    }

    /**
     * <h4>the main method is the starting point for RaziFX program</h4>
     *
     * @param args This signifies that args is an array of objects of the String
     * class. This array is used to store command-line arguments that are passed
     * to the Java program when it's executed.
     */
    public static void main(String[] args) {
        // check Internet connection
        InternetChecker checker = new InternetChecker();
        checker.start();
        // luanch application
        LauncherImpl.launchApplication(Main.class, RaziPreloader.class, args);
        checker.stop(); // Stop the connection checker when exiting
        RaziLogger.info("Application closed.");
    }

    /**
     * logout: This method is called when the user clicks the exit button on the
     * window and warns the user to make sure to save the information.
     *
     * @param stage Current stage
     */
    public void logout(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("خروج");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setContentText("Please make sure to save the data before exiting.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            RaziLogger.info("Application closed by user.");
            stage.close();
            System.exit(0);
        }
    }

}
