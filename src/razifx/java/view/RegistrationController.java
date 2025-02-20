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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import razifx.core.FirstTimeRunCheck;
import razifx.core.RaziLogger;
import razifx.java.model.dao.SuperUserDAO;
import razifx.java.model.entity.SuperUser;

/**
 * RegistrationController.java
 * 
 * @deprecated Use UserController instead of this class
 * @author mahdihoseinzade
 * @since 1.0
 */
@Deprecated
public class RegistrationController {

    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField companyNameTextField;

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String companyName = companyNameTextField.getText();
        validationForm();
        // Create a new SuperUser object (replace with your actual logic)
        SuperUser superUser = new SuperUser();
        superUser.setFirstName(firstName);
        superUser.setLastName(lastName);
        superUser.setCompanyName(companyName);
        // Save the SuperUser to the database (replace with your SuperUserDAO logic)
        boolean success = SuperUserDAO.save(superUser); 
        if (success) {
            // Show success message and load the main application scene
            showAlert("ثبت شد", "SuperUser registered successfully!");
            try {
                // Create RaziFX.txt file in logs directory.
                FirstTimeRunCheck.successfulSubmitSuperUserInDatabase();
                URL url = new File("src/razifx/resources/fxml/job_form.fxml").toURI().toURL();
                Parent root = FXMLLoader.load(url);
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("شغل ها");
                stage.setResizable(false);
            } catch (Exception e) {
                RaziLogger.error(getClass().getName().toString(), e);
                showAlert("خطا", "Failed to load main application scene.");
            }
        } else {
            RaziLogger.warn(RegistrationController.class.getName() + "Failed to register SuperUser.");
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            System.exit(1);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
    
    private void validationForm() {
        // Validate input (e.g., check for empty fields)
        if (firstNameTextField.getText().isEmpty() || lastNameTextField.getText().isEmpty() ||
                companyNameTextField.getText().isEmpty()) {
            showAlert("خطا", "Please fill in all fields.");
            return;
        }
    }
}