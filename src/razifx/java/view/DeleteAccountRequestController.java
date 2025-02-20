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

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import razifx.core.RaziLogger;
import razifx.java.model.dao.DBConnector;

/**
 * FXML Controller class
 *
 * @author mahdihoseinzade
 */
public class DeleteAccountRequestController implements Initializable {

    @FXML
    private TextArea descriptionsTextArea;
    @FXML
    private Button submit_request_button;

    private DBConnector db;

    private Long user_id;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The delete account request openned.");
        db = new DBConnector();

        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
    }

    @FXML
    private void handleSubmitReq(ActionEvent event) {
        if (descriptionsTextArea.getText().isEmpty()) {
            showAlert("لطفا دلیل خود را شرح دهید", "Please write your the reason of delete account.", Alert.AlertType.WARNING);
            return;
        }
        Platform.runLater(() -> {
            try (Connection connection = db.connect()) {

                Long userId = user_id; // The user ID making the request
                String description = descriptionsTextArea.getText(); // The user's description

                String sql = "INSERT INTO delete_account_requests (user_id, description) VALUES (?, ?)";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, userId);
                    statement.setString(2, description);

                    int rowsInserted = statement.executeUpdate();

                    if (rowsInserted > 0) {
                        showAlert("درخواست شما ثبت شد", "your delete request submit", Alert.AlertType.INFORMATION);
                        Stage stage = (Stage) submit_request_button.getScene().getWindow();
                        stage.close();
                    } else {
                        showAlert("خطا", null, Alert.AlertType.ERROR);
                        return;
                    }
                }

            } catch (SQLException e) {
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                showAlert("خطا", "Failed to load main application scene.", Alert.AlertType.ERROR);
                return;
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
