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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
import razifx.java.model.entity.Employee;
import razifx.java.model.entity.Leave;

/**
 * LeaveRegistrationController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class LeaveRegistrationController implements Initializable {

    @FXML
    private TableView<Leave> leavesTable;

    @FXML
    private TableColumn<Leave, Long> employeeIdColumn;

    @FXML
    private TableColumn<Leave, String> startDateColumn;

    @FXML
    private TableColumn<Leave, String> endDateColumn;

    @FXML
    private TableColumn<Leave, String> leaveTypeColumn;

    @FXML
    private TextField employeeIdField;

    @FXML
    private ComboBox<String> leaveTypeComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button move_main;

    @FXML
    private TextField full_name_field;

    @FXML
    private TextField national_id_field;

    @FXML
    private TextField birthdate_field;

    @FXML
    private TextField phone_number_field;

    @FXML
    private TextField address_field;

    private ObservableList<Leave> leavesData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Employee employee;
    private Long user_id;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private TextField dayOfDateField1;
    @FXML
    private ComboBox<String> mounthOfDateField1;
    @FXML
    private TextField yearOfDateField1;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The leave - leave registration stage start.");
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
        // Load initial data from database
        loadLeaves();
        // Initialize table columns
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEmployeeID"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedStartDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEndDate"));
        leaveTypeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedLeaveType"));

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        // Initialize month combobox     
        mounthOfDateField1.getItems().removeAll(mounthOfDateField1.getItems());
        for (int i = 0; i < 12; i++) {
            mounthOfDateField1.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField1.getSelectionModel().select(monthOfYear[0]);

        // Initialize expenseTypeComboBox
        leaveTypeComboBox.getItems().removeAll(leaveTypeComboBox.getItems());
        leaveTypeComboBox.getItems().addAll("بیمار", "معمولی", "دیگر");
        leaveTypeComboBox.getSelectionModel().select("معمولی");
    }

    void setEmployee(Employee e) {
        if (e != null) {
            employee = e;
            full_name_field.setText(employee.getFullName());
            national_id_field.setText(employee.getNationalId());
            birthdate_field.setText(employee.getFormattedDate());
            phone_number_field.setText(employee.getPhoneNumber());
            address_field.setText(employee.getAddress());
            employeeIdField.setText(employee.getEmployeeId().toString());

        }
        loadLeaves();
    }

    private void loadLeaves() {
        if (employee != null) {
            Long employee_id = employee.getEmployeeId();
            try {
                leavesData = FXCollections.observableArrayList();
                String query = "SELECT * FROM leaves WHERE employee_id=? AND user_id=?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setLong(1, employee_id);
                preparedStatement.setLong(2, user_id);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Leave leave = new Leave();
                    leave.setEmployee(this.employee);
                    leave.setStartDate(resultSet.getDate("start_date"));
                    leave.setEndDate(resultSet.getDate("end_date"));
                    leave.setLeaveType(Leave.LeaveType.valueOf(resultSet.getString("leave_type")));
                    leavesData.add(leave);
                }
            } catch (SQLException e) {
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                return;
            }

            leavesTable.setItems(leavesData);
        }
    }

    @FXML
    void saveLeave(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        try {
            Long employeeId = employee.getEmployeeId();
            // convert date
            String day = dayOfDateField.getText();
            String month = mounthOfDateField.getValue().toString();
            String year = yearOfDateField.getText();
            if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
                showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
                return;
            }
            int numberOfMonth = DateConvertor.getMonthOfYear(month);
            LocalDate startDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

            // convert date
            String day1 = dayOfDateField1.getText();
            String month1 = mounthOfDateField1.getValue().toString();
            String year1 = yearOfDateField1.getText();
            if (!RegexValidator.isNumericDay(day1) || !RegexValidator.isNumericYear(year1)) {
                showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
                return;
            }
            int numberOfMonth1 = DateConvertor.getMonthOfYear(month1);
            LocalDate endDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year1), numberOfMonth1, Integer.parseInt(day1));
            String leaveType = leaveTypeComboBox.getValue();
            switch (leaveType) {
                case "معمولی":
                    leaveType = "CASUAL";
                    break;
                case "دیگر":
                    leaveType = "OTHER";
                    break;
                case "بیمار":
                    leaveType = "SICK";
                    break;
            }
            preparedStatement = connection.prepareStatement("INSERT INTO leaves (employee_id, start_date, end_date, leave_type, user_id) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, employeeId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(3, java.sql.Date.valueOf(endDate));
            preparedStatement.setString(4, leaveType);
            preparedStatement.setLong(5, user_id);
            preparedStatement.executeUpdate();
            // Clear fields and refresh table
            clearFields();
            loadLeaves();
            leavesTable.refresh();
        } catch (SQLException e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    void deleteLeave(ActionEvent event) {
        if (leavesTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک مرخصی را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        try {
            Leave selectedLeave = leavesTable.getSelectionModel().getSelectedItem();
            if (selectedLeave != null) {
                preparedStatement = connection.prepareStatement("DELETE FROM leaves WHERE employee_id = ? AND user_id=?");
                preparedStatement.setLong(1, selectedLeave.getEmployee().getEmployeeId());
                preparedStatement.setLong(2, user_id);
                preparedStatement.executeUpdate();
                // Remove from table and refresh
                leavesData.remove(selectedLeave);
                leavesTable.refresh();
            }
        } catch (SQLException e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    private void clearFields() {
        employeeIdField.setText(employee.getEmployeeId().toString());
        dayOfDateField.setText("01");
        dayOfDateField1.setText("01");
        yearOfDateField.setText("1403");
        yearOfDateField1.setText("1403");
    }

    private boolean validationForm() {
        if (employeeIdField.getText().isEmpty() || dayOfDateField.getText().isEmpty()
                || dayOfDateField1.getText().isEmpty() || mounthOfDateField.getValue() == null
                || mounthOfDateField1.getValue() == null || yearOfDateField.getText().isEmpty()
                || yearOfDateField1.getText().isEmpty()) {
            return false;
        }
        return true;
    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/leaves.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("مرخصی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا", "Failed to load leaves application scene.", Alert.AlertType.ERROR);
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
    private void printReport(ActionEvent event) {
        if (leavesData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT l.leave_id, l.start_date, l.end_date, l.leave_type, e.phone_number\n"
                        + "FROM leaves l\n"
                        + "JOIN employees e ON l.employee_id = e.employee_id\n"
                        + "WHERE l.user_id = " + user_id + " AND e.user_id = " + user_id + " AND l.employee_id = " + employee.getEmployeeId() + ";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/leaves.jrxml");
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
