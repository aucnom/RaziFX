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

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import static razifx.core.Hashs.hashPassword;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.java.model.dao.DBConnector;

/**
 * razifx.java.view.login.SignUp SignUp.java
 *
 * @author mahdihoseinzade
 * @since 1.0.1
 */
public class SignUp implements Initializable {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField companyNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox privacyTermsCheckBox;
    @FXML
    private Button signUpButton;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The user entered sign up window.");
        // Initialize database connection
        db = new DBConnector();
        connection = db.connect();
    }

    /**
     * The "signup" method in software refers to the process of creating a new
     * user account within an application or system. It's the initial step for
     * new users to gain access to the software's features and functionalities.
     *
     * @param event Handle button clicks: When a button is pressed, an
     * ActionEvent is generated.
     */
    @FXML
    private void signUp(ActionEvent event) {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()
                || emailField.getText().isEmpty() || firstNameField.getText().isEmpty()
                || lastNameField.getText().isEmpty() || companyNameField.getText().isEmpty()) {
            showAlert("لطفا موارد خواسته شده را پر کنید", "Please enter the required field with correct info.", Alert.AlertType.WARNING);
            return;
        }
        try {
            if (!RegexValidator.isValidEmail(emailField.getText())) {
                showAlert("فقط ایمیل گوگل", "please insert gmail account.", Alert.AlertType.WARNING);
                return;
            }
            String username = usernameField.getText();
            if (username.length() < 5) {
                showAlert("نام کاربری حداقل پنج کاراکتر باید باشد", "Please choose a username with atleast 5 chars.", Alert.AlertType.WARNING);
                return;
            }
            if (!RegexValidator.isValidPassword(passwordField.getText())) {
                showAlert("رمزعبور ضعیف", "Please Insert a strong password.", Alert.AlertType.WARNING);
                return;
            }
            if (!privacyTermsCheckBox.isSelected()) {
                showAlert("لطفا ضوابط را بپذیرید.", "Please select policy and terms.", Alert.AlertType.WARNING);
                return;
            }
            String password = hashPassword(passwordField.getText()); // Hash the password
            String email = emailField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String companyName = companyNameField.getText();

            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // Username already exists
                showAlert("نام کاربری دیگری را انتخاب کنید", "Please choose another username.", Alert.AlertType.WARNING);
                RaziLogger.info("The user entered username which has exist.");
                return;
            }

            preparedStatement = connection.prepareStatement("INSERT INTO users (username, password, email, first_name, last_name, company_name) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, firstName);
            preparedStatement.setString(5, lastName);
            preparedStatement.setString(6, companyName);
            preparedStatement.executeUpdate();

            // Clear fields after successful sign-up
            clearFields();
            showAlert("حساب کاربری ساخته شد", "Your account created successful, You can sigin now.", Alert.AlertType.INFORMATION);
            RaziLogger.info("The user create a new account successful.");
            // close stage
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            if (e.getMessage().contains("Duplicate")) {
                RaziLogger.warn("Duplicate entry 'test@gmail.com' for key 'users.email'.");
                showAlert("آدرس ایمیل دیگری را وارد کنید", "Please enter another email address.", Alert.AlertType.WARNING);
            } else {
                RaziLogger.warn("An issue occured during creating account for user.");
                showAlert("خطا در بارگذاری اطلاعات", "Please check your internet account.", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * It's a common utility function used to clear user input, either after a
     * successful submission, to allow the user to start over, or as part of a
     * "reset" button functionality.
     */
    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        companyNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    /**
     * In JavaFX, an Alert is a dialog box that is used to display information
     * to the user or to get a response from the user. Alerts can be used to
     * display error messages, warnings, or confirmations.
     *
     * @param title
     * @param message display informations.
     * @param type types of alerts, each with a different purpose.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
