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
import java.util.ResourceBundle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import org.controlsfx.control.Notifications;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.BankAccount;
import razifx.java.model.entity.Employee;
import razifx.java.model.entity.Salary;

/**
 * SalaryRegController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class SalaryRegController implements Initializable {

    @FXML
    private TableView<Salary> salariesTable;

    @FXML
    private TableColumn<Salary, Long> salaryIdColumn;

    @FXML
    private TableColumn<Salary, Long> employeeIdColumn;

    @FXML
    private TableColumn<Salary, String> amountColumn;

    @FXML
    private TableColumn<Salary, String> payDateColumn;

    @FXML
    private TextField employeeIdField;

    @FXML
    private TextField amountField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Salary> salariesData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

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

    private Employee employee;

    private Long user_id;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private Button clear_form;
    @FXML
    private Button reportButton;
    @FXML
    private ComboBox<String> bankAccountListCombo;

    private ArrayList<BankAccount> bankAccounts;
    @FXML
    private TextField countLeavesDay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The salary registration stage start.");
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        loadBankAccount();
        bankAccountListCombo.getItems().removeAll(bankAccountListCombo.getItems());
        if (bankAccounts != null) {
            bankAccounts.stream().forEach(ba -> {
                bankAccountListCombo.getItems().add(ba.getCardNumber());
            });
        } else {
            showAlert("ابتدا حساب بانکی تعریف کنید", "Please intial a bank account first.", Alert.AlertType.INFORMATION);
            return;
        }

        // Initialize table columns
        salaryIdColumn.setCellValueFactory(new PropertyValueFactory<>("salaryId"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEmployeeID"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        payDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        // Load initial data from database
        loadSalaries();

        Platform.runLater(() -> {
            try {
                // Call the stored procedure
                try (CallableStatement statement = connection.prepareCall("{CALL calculate_leave_days(?)}")) {
                    statement.setLong(1, employee.getEmployeeId()); // Set the employee ID parameter

                    // Execute the stored procedure and get the result set
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int totalLeaveDays = resultSet.getInt("total_leave_days");
                            countLeavesDay.setText(totalLeaveDays-1 + " روز");
                        } else {
                            countLeavesDay.setText("مرخصی ندارد -");
                        }
                    }
                }

            } catch (SQLException e) {
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void loadBankAccount() {
        bankAccounts = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM bank_accounts WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                BankAccount bankAccount = new BankAccount();
                bankAccount.setId(resultSet.getLong("bank_account_id"));
                bankAccount.setBankName(resultSet.getString("bank_name"));
                bankAccount.setAccountNumber(resultSet.getString("account_number"));
                bankAccount.setCardNumber(resultSet.getString("card_number"));
                bankAccount.setAccountHolderName(resultSet.getString("account_holder_name"));
                bankAccount.setBalance(resultSet.getBigDecimal("balance"));
                bankAccount.setDescription(resultSet.getString("description"));
                bankAccounts.add(bankAccount);
            }
        } catch (SQLException e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
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
        loadSalaries();
    }

    private void loadSalaries() {
        if (employee != null) {
            Task<ObservableList<Salary>> loadSalariesTask = new Task<ObservableList<Salary>>() {
                @Override
                protected ObservableList<Salary> call() throws Exception {
                    ArrayList<Salary> salaryList = new ArrayList<>();
                    try {
                        salariesData = FXCollections.observableArrayList();
                        preparedStatement = connection.prepareStatement("SELECT * FROM salaries WHERE employee_id=? AND user_id = ?");
                        preparedStatement.setLong(1, employee.getEmployeeId());
                        preparedStatement.setLong(2, user_id);
                        resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            Salary salary = new Salary();
                            salary.setSalaryId(resultSet.getLong("salary_id"));
                            salary.setEmployee(employee);
                            salary.setAmount(resultSet.getBigDecimal("amount"));
                            salary.setPayDate(resultSet.getDate("pay_date"));
                            salaryList.add(salary);
                        }
                    } catch (SQLException e) {
                        throw new Exception(getClass().getName() + " at line 249");
                    }
                    return FXCollections.observableArrayList(salaryList);
                }
            };
            loadSalariesTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent arg0) {
                    Throwable throwable = loadSalariesTask.getException();
                    RaziLogger.error(throwable.getMessage());
                    showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                    return;
                }
            });
            loadSalariesTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    salariesData = loadSalariesTask.getValue();
                    salariesTable.setItems(salariesData);
                });
            });
            new Thread(loadSalariesTask).start();
        }
    }

    @FXML
    void saveSalary(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }

        long employeeId = employee.getEmployeeId();
        if (!RegexValidator.isValidAmount(amountField.getText())) {
            showAlert("مبلغ نادرست", "Please enter the correct money number", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal amount = new BigDecimal(amountField.getText());
        // convert date
        String day = dayOfDateField.getText();
        String month = mounthOfDateField.getValue().toString();
        String year = yearOfDateField.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }

        String cardNumber = bankAccountListCombo.getValue().toString();
        Long bai = null; // bank account id
        for (BankAccount ba : bankAccounts) {
            if (cardNumber.equals(ba.getCardNumber())) {
                bai = ba.getId();
                break;
            }
        }
        Long bank_account_id = bai;

        int numberOfMonth = DateConvertor.getMonthOfYear(month);
        LocalDate gDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Long transactionCode = null;
                    // new Transaction
                    PreparedStatement addTransaction = connection.prepareStatement("INSERT INTO transactions (user_id, account_id, transaction_date, transaction_type, amount) VALUES (?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    addTransaction.setLong(1, user_id);
                    addTransaction.setLong(2, bank_account_id);
                    addTransaction.setDate(3, java.sql.Date.valueOf(gDate));
                    addTransaction.setString(4, "salary");
                    addTransaction.setBigDecimal(5, amount);
                    int rowsInserted = addTransaction.executeUpdate();
                    if (rowsInserted > 0) {
                        try (ResultSet generatedKeys = addTransaction.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                transactionCode = generatedKeys.getLong(1);
                            }
                        }
                    }
                    RaziLogger.info("A transaction submitted successful.");
                    preparedStatement = connection.prepareStatement("INSERT INTO salaries (employee_id, amount, pay_date, user_id, transaction_id) VALUES (?, ?, ?, ?, ?)");
                    preparedStatement.setLong(1, employeeId);
                    preparedStatement.setBigDecimal(2, amount);
                    preparedStatement.setDate(3, java.sql.Date.valueOf(gDate));
                    preparedStatement.setLong(4, user_id);
                    preparedStatement.setLong(5, transactionCode);
                    preparedStatement.executeUpdate();
                    RaziLogger.info("A transation submit successful.");
                    // update balance
                    PreparedStatement substatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance - ? WHERE bank_account_id = ? AND user_id=?");
                    substatement.setBigDecimal(1, amount);
                    substatement.setLong(2, bank_account_id);
                    substatement.setLong(3, user_id);
                    substatement.executeUpdate();

                    // Clear fields and refresh table
                    clearFields();
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 341");
                }
                return null; // end of task
            }
        };
        saveTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = saveTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        saveTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("RaziFX")
                        .text("The data submitted to database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                loadSalaries();
                salariesTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deleteSalary(ActionEvent event) {
        if (salariesTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک حقوق راانتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }        
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Long transactionCode = null;
                try {
                    Salary selectedSalary = salariesTable.getSelectionModel().getSelectedItem();
                    if (selectedSalary != null) {
                        // Find transaction id
                        String selectSql = "SELECT transaction_id FROM salaries WHERE salary_id = ? AND user_id=?";
                        PreparedStatement foundTransaction = connection.prepareStatement(selectSql);
                        foundTransaction.setLong(1, selectedSalary.getSalaryId());
                        foundTransaction.setLong(2, user_id);
                        try (ResultSet resultSet = foundTransaction.executeQuery()) {
                            if (resultSet.next()) {
                                transactionCode = resultSet.getLong("transaction_id");
                            } else {
                                throw new SQLException("No transaction found with ID " + selectedSalary.getSalaryId());
                            }
                        }

                        preparedStatement = connection.prepareStatement("DELETE FROM salaries WHERE salary_id = ? AND user_id = ?");
                        preparedStatement.setLong(1, selectedSalary.getSalaryId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();

                        // Found amount to add it to balance
                        BigDecimal amount = null;
                        Long bank_account_id = null;
                        PreparedStatement delStatement = connection.prepareStatement("SELECT amount, account_id FROM transactions WHERE transaction_id = ? AND user_id=?");
                        delStatement.setLong(1, transactionCode);
                        delStatement.setLong(2, user_id);
                        try (ResultSet resultSet = delStatement.executeQuery()) {
                            if (resultSet.next()) {
                                amount = resultSet.getBigDecimal("amount");
                                bank_account_id = resultSet.getLong("account_id");
                            }
                        }

                        // update balance
                        PreparedStatement upStatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance + ? WHERE bank_account_id = ? AND user_id=?");
                        upStatement.setBigDecimal(1, amount);
                        upStatement.setLong(2, bank_account_id);
                        upStatement.setLong(3, user_id);
                        upStatement.executeUpdate();

                        // delete transaction
                        PreparedStatement delTransaction = connection.prepareStatement("DELETE FROM transactions WHERE transaction_id = ? AND user_id=?");
                        delTransaction.setLong(1, transactionCode);
                        delTransaction.setLong(2, user_id);
                        delTransaction.executeUpdate();
                        RaziLogger.info("The transaction with id " + transactionCode + " deleted successful.");
                        // Remove from table and refresh
                        salariesData.remove(selectedSalary);
                        loadSalaries();
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 341");
                }
                return null; // end of task
            }
        };
        deleteTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = deleteTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        deleteTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("RaziFX")
                        .text("The data deleted from database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                salariesTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        employeeIdField.setText(employee.getEmployeeId().toString());
        amountField.setText("0");
        dayOfDateField.setText("01");
        yearOfDateField.setText("1403");
    }

    private boolean validationForm() {
        if (employeeIdField.getText().isEmpty() || amountField.getText().isEmpty()
                || dayOfDateField.getText().isEmpty() || yearOfDateField.getText().isEmpty()
                || bankAccountListCombo.getValue() == null) {
            return false;
        }
        return true;
    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/salaries.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("حقوق و دستمزد");
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
    private void clearFieledButton(ActionEvent event) {
        clearFields();
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (salariesData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT\n"
                        + "    s.salary_id,\n"
                        + "    e.first_name,\n"
                        + "    e.last_name,\n"
                        + "    j.title AS job_title,\n"
                        + "    s.amount,\n"
                        + "    s.pay_date\n"
                        + "FROM\n"
                        + "    salaries s\n"
                        + "JOIN\n"
                        + "    employees e ON s.employee_id = e.employee_id\n"
                        + "JOIN\n"
                        + "    jobs j ON e.job_id = j.job_id\n"
                        + "WHERE\n"
                        + "    s.user_id = " + user_id + " AND s.employee_id = " + employee.getEmployeeId() + ";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/salaries_p.jrxml");
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
