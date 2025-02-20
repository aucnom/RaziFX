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

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.ChecksReceived;
import razifx.java.model.entity.Customer;

/**
 * CheckDepositController.java
 *
 * @author mahdihoseinzade
 * @since 1.1
 */
// Controller class (CheckDepositController.java)
public class CheckDepositController implements Initializable {

    @FXML
    private ComboBox<String> checkNumbersCombobox;
    @FXML
    private TextField customerNameTextField;
    @FXML
    private TextField checkAmountTextField;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private Button updateButton;

    private Long user_id;
    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ChecksReceived selectedChecksReceived;
    private ArrayList<Customer> customers;
    private ArrayList<ChecksReceived> checks;

    @FXML
    private Button selectCheckButton;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The update deposit date - check received stage openned.");
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }

        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        loadCustomers();

        loadChecks();
        checkNumbersCombobox.getItems().removeAll(checkNumbersCombobox.getItems());
        checks.stream().forEach((c) -> {
            checkNumbersCombobox.getItems().add(c.getCheckId() + "/" + c.getCheckNumber());
        });
        checkNumbersCombobox.getSelectionModel().select(checks.get(1).getCheckNumber());
    }

    private void loadChecks() {
        try {
            checks = new ArrayList<>();
            preparedStatement = connection.prepareStatement("SELECT * FROM checks_received WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ChecksReceived check = new ChecksReceived();
                Customer customer = null;
                long customer_id = resultSet.getLong("customer_id");
                check.setCheckId(resultSet.getLong("check_id"));
                for (Customer c : customers) {
                    if (customer_id == c.getCustomerId()) {
                        customer = c;
                    }
                }
                check.setCustomer(customer);
                check.setCheckNumber(resultSet.getString("check_number"));
                check.setCheckDate(resultSet.getDate("check_date"));
                check.setAmount(resultSet.getBigDecimal("amount"));
                checks.add(check);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadCustomers() {
        try {
            customers = new ArrayList<>();
            preparedStatement = connection.prepareStatement("SELECT * FROM customers WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(resultSet.getLong("customer_id"));
                customer.setFirstName(resultSet.getString("first_name"));
                customer.setLastName(resultSet.getString("last_name"));
                customer.setNationalId(resultSet.getString("national_id"));
                customer.setBirthdate(resultSet.getDate("birthdate"));
                customer.setPhoneNumber(resultSet.getString("phone_number"));
                customer.setAddress(resultSet.getString("address"));
                customer.setGender(Customer.Gender.valueOf(resultSet.getString("gender")));
                customers.add(customer);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    @FXML
    private void handleSetCheckNumber(ActionEvent event) {
        if (checkNumbersCombobox.getValue() == null) {
            showAlert("یک چک را انتخاب کنید", "Please select a check.", Alert.AlertType.WARNING);
            return;
        }
        String check_number = checkNumbersCombobox.getValue().toString();
        for (ChecksReceived findSelectedChecksReceived : checks) {
            if (check_number.equals(findSelectedChecksReceived.getCheckId() + "/" + findSelectedChecksReceived.getCheckNumber())) {
                selectedChecksReceived = findSelectedChecksReceived;
                customerNameTextField.setText(findSelectedChecksReceived.getCustomer().getFullName());
                checkAmountTextField.setText(findSelectedChecksReceived.getFormattedAmount());
            }
        }
    }

    @FXML
    private void handleAddDepositDate(ActionEvent event) {
        if (checkAmountTextField.getText().isEmpty() || customerNameTextField.getText().isEmpty()) {
            showAlert("یک چک را انتخاب کنید", "Please select a check.", Alert.AlertType.WARNING);
            return;
        }
        if (dayOfDateField.getText().isEmpty() || mounthOfDateField.getValue() == null
                || yearOfDateField.getText().isEmpty()) {
            showAlert("تاریخ را بصورت کامل وارد کنید.", "Please enter the date completely.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "UPDATE checks_received SET deposit_date = ? WHERE check_id = ? AND user_id = ?";
        Long check_id = selectedChecksReceived.getCheckId();

        // convert date
        String day = dayOfDateField.getText();
        String month = mounthOfDateField.getValue().toString();
        String year = yearOfDateField.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth = DateConvertor.getMonthOfYear(month);
        LocalDate gDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, Date.valueOf(gDate)); // Set the deposit date
            preparedStatement.setLong(2, check_id); // Set the check ID
            preparedStatement.setLong(3, user_id);

            int rowsAffected = preparedStatement.executeUpdate(); // Execute the update

            if (rowsAffected > 0) {
                showAlert("عملیات موفق", "the check with check id " + check_id + "updated successful.", Alert.AlertType.INFORMATION);
                return;
            } else {
                showAlert("عملیات ناموفق", "the check with check id " + check_id + "failed to update.", Alert.AlertType.INFORMATION);
                return;
            }
        } catch (SQLException ex) {
            Logger.getLogger(CheckDepositController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void printReport(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT check_id, customer_id, check_number, check_date, amount, deposit_date, user_id\n"
                        + "FROM checks_received\n"
                        + "WHERE deposit_date IS NOT NULL AND user_id = " + user_id + ";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/deposit_report.jrxml");
                JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);

                // Set Parameters (if any)
                Map<String, Object> parameters = new HashMap<>();

                // Fill Report
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

                // Display Report in a new window (JasperViewer)
                JasperViewer.viewReport(jasperPrint, false); // false: close on last page, true: exit application
            } catch (Exception e) {
                Logger.getLogger(EmployeesController.class.getName()).log(Level.SEVERE, null, e);
                RaziLogger.error(e.getMessage() + "\n" + getClass().getName());
                RaziLogger.error("An error occured during reporting.");
                showAlert("خطا", "Failed to Obtain data from db.", Alert.AlertType.ERROR);
            }
        });
    }
}
