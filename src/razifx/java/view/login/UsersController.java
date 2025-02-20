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
package razifx.java.view.login;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import razifx.core.FirstRunCheck;
import static razifx.core.Hashs.hashPassword;
import razifx.core.RaziLogger;
import razifx.core.data.AppInfo;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.User;

/**
 * FXML Controller class razifx.java.view.login.UsersController
 * UsersController.java
 *
 * @author mahdihoseinzade
 * @since 1.0.1
 */
public class UsersController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public static User currentUser;
    @FXML
    private Label appVersionLabel;
    @FXML
    private Button moveToSignUp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("Application start successful.");
        // Initialize database connection
        db = new DBConnector();
        connection = db.connect();

        appVersionLabel.setText(AppInfo.getAppVersion());
    }

    /**
     * a "sign-in" method refers to the process of verifying a user's identity
     * before granting them access to the application or specific features. It's
     * a crucial security measure that protects user data and ensures only
     * authorized individuals can use the software.  
     *
     * @param event Handle button clicks: When a button is pressed, an
     * ActionEvent is generated.
     * @throws IOException
     */
    @FXML
    void signIn(ActionEvent event) throws IOException {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("اطلاعات حساب کاربری خود را وارد کنید", "Please enter your account's username and password.", Alert.AlertType.WARNING);
            return;
        }
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            preparedStatement = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // It checks whether the hashed password stored in the database matches the user's password when logging in.
                String storedPassword = resultSet.getString("password");
                if (hashPassword(password).equals(storedPassword)) {
                    // Successful login
                    currentUser = new User();
                    currentUser.setUserName(username);
                    currentUser.setPassword(password);

                    java.util.Date date = new java.util.Date();
                    // Login information, including username, IP address, and date and time of login, is stored in the database.
                    Task<Void> saveLoginInfo = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            String ipAddress = getIPAddress(); // Implement IP address retrieval logic
                            preparedStatement = connection.prepareStatement("INSERT INTO login_logs (username, login_time, ip_address) VALUES (?, ?, ?)");
                            preparedStatement.setString(1, username);
                            preparedStatement.setTimestamp(2, new Timestamp(date.getTime()));
                            preparedStatement.setString(3, ipAddress);
                            preparedStatement.executeUpdate();
                            RaziLogger.info("The client info submit successful.");
                            return null;
                        }
                    };
                    new Thread(saveLoginInfo).start();
                    Long user_id = null;
                    preparedStatement = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
                    preparedStatement.setString(1, username);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        user_id = resultSet.getLong("user_id");
                    }
                    if (FirstRunCheck.isFirstRun(user_id)) {
                        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/job_form.fxml"));
                        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("شغل ها");
                        stage.setResizable(false);
                        stage.centerOnScreen();
                    } else {
                        // If SuperUser exists, load the main application scene
                        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/Main.fxml"));
                        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("نرم افزار حسابداری رازی");
                        stage.setResizable(false);
                        stage.centerOnScreen();
                    }
                } else {
                    // Incorrect password
                    showAlert("نام کاربری یا رمزعبور اشتباه", "Wrong username or password..", Alert.AlertType.WARNING);
                    RaziLogger.info("The user entered wrong username or password.");
                    return;
                }
            } else {
                // User not found
                showAlert("نام کاربری یا رمزعبور اشتباه", "Wrong username or password..", Alert.AlertType.WARNING);
                RaziLogger.info("The user entered wrong username or password.");
                return;
            }

        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
        }
    }

    /**
     * It's a common utility function used to clear user input, either after a
     * successful submission, to allow the user to start over, or as part of a
     * "reset" button functionality.
     */
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * This method is responsible for finding the user's public IP address.
     *
     * @return User's public IP address
     */
    private String getIPAddress() {
        String publicIpAddress = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            int i = 0;
            outer:
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                inner:
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (i == 7) {
                        publicIpAddress = inetAddress.getHostAddress();
                        break outer;
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            RaziLogger.warn("The public ip address cannot find.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return String.format("%s", "An error occurred while finding the user's IP address.");
        }
        return publicIpAddress;
    }

    /**
     * Display sign up window
     *
     * @param event Handle button clicks: When a button is pressed, an
     * ActionEvent is generated.
     */
    @FXML
    private void handleMoveToSignUp(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/login/signup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("ساخت حساب کاربری");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            showAlert("خطا", "Failed to load main application scene.", Alert.AlertType.ERROR);
            return;
        }
    }
}
