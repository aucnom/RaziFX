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

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * FXML Controller class
 *
 * @author mahdihoseinzade
 * @since 1.0.10
 */
public class SelectFiscalYearController implements Initializable {

    @FXML
    private Button move_main;
    @FXML
    private TextField dayOfDateField1;
    @FXML
    private ComboBox<String> mounthOfDateField1;
    @FXML
    private TextField yearOfDateField1;
    @FXML
    private TextField dayOfDateField2;
    @FXML
    private ComboBox<String> mounthOfDateField2;
    @FXML
    private TextField yearOfDateField2;
    @FXML
    private Button calculateButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The bussines reporting - set fiscal year start.");
        // Initialize month combobox
        mounthOfDateField1.getItems().removeAll(mounthOfDateField1.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField1.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField1.getSelectionModel().select(monthOfYear[0]);

        // Initialize month combobox
        mounthOfDateField2.getItems().removeAll(mounthOfDateField2.getItems());
        for (int i = 0; i < 12; i++) {
            mounthOfDateField2.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField2.getSelectionModel().select(monthOfYear[0]);
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
            showAlert("خطا", "Failed to load Main application scene.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void calculating(ActionEvent event) {
        if (yearOfDateField1.getText().isEmpty() || yearOfDateField2.getText().isEmpty()
                || mounthOfDateField1.getValue() == null || mounthOfDateField2.getValue() == null
                || dayOfDateField1.getText().isEmpty() || dayOfDateField2.getText().isEmpty()) {
            showAlert("تاریخ را بصورت کامل وارد کنید", "Please enter the start and end date completely.", Alert.AlertType.WARNING);
            return;
        }
        // convert start date
        String day = dayOfDateField1.getText();
        String month = mounthOfDateField1.getValue().toString();
        String year = yearOfDateField1.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth = DateConvertor.getMonthOfYear(month);
        LocalDate startDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

        // convert end date
        String day2 = dayOfDateField2.getText();
        String month2 = mounthOfDateField2.getValue().toString();
        String year2 = yearOfDateField2.getText();
        if (!RegexValidator.isNumericDay(day2) || !RegexValidator.isNumericYear(year2)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth2 = DateConvertor.getMonthOfYear(month2);
        LocalDate endDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year2), numberOfMonth2, Integer.parseInt(day2));
        if (Integer.parseInt(year)>Integer.parseInt(year)) {
            showAlert("تاریخ اشتباه", "the begin date is greater than end date and not valid!!", Alert.AlertType.WARNING);
            return;
        }
        
        if (startDate.equals(endDate)) {
            showAlert("تاریخ اشتباه", "the same date not valid!!!!!!!", Alert.AlertType.WARNING);
            return;
        }
        String start_date_s = year + " ";
        String end_date_s = year2 + " ";
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("razifx/resources/fxml/expense_income.fxml"));
            Parent root = (Parent) loader.load();
            ExpenseIncomeController eic = loader.getController();
            if (endDate!=null && startDate!=null) { eic.setFiscalYear(startDate, endDate, start_date_s,end_date_s); }
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("گزارش سالیانه");
            stage.setResizable(false);
        } catch (IOException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }

    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
