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
import java.util.List;
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
import razifx.java.model.entity.Expense;

/**
 * ExpensesController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class ExpensesController implements Initializable {

    @FXML
    private TableView<Expense> expensesTable;

    @FXML
    private TableColumn<Expense, Long> expenseIdColumn;

    @FXML
    private TableColumn<Expense, String> expenseDateColumn;

    @FXML
    private TableColumn<Expense, String> amountColumn;

    @FXML
    private TableColumn<Expense, String> expenseTypeColumn;

    @FXML
    private TableColumn<Expense, String> descriptionColumn;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> expenseTypeComboBox;

    @FXML
    private TextField descriptionField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Expense> expensesData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @FXML
    private Button move_main;

    @FXML
    private Button clear_form;

    private Long user_id;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private Button reportButton;
    @FXML
    private ComboBox<String> bankAccountListCombo;

    private ArrayList<BankAccount> bankAccounts;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The expense management stage start.");
        db = new DBConnector();
        connection = db.connect();

        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
        // Initialize table columns
        expenseIdColumn.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        expenseTypeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedType"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Initialize expenseTypeComboBox
        expenseTypeComboBox.getItems().removeAll(expenseTypeComboBox.getItems());
        expenseTypeComboBox.getItems().addAll("تعمیرات", "پرداخت قبوض", "دیگر");
        expenseTypeComboBox.getSelectionModel().select("دیگر");
        // Load initial data from database
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

        loadExpenses();

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        /**
         * load data from table when user double clicked on it.
         */
        expensesTable.setRowFactory(tv -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Expense data = row.getItem();
                    amountField.setText(data.getAmount().toString());
                    descriptionField.setText(data.getDescription());
                }
            });
            return row;
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
            return;
        }
    }

    private void loadExpenses() {
        Task<ObservableList<Expense>> loadExpenseTask = new Task<ObservableList<Expense>>() {
            @Override
            protected ObservableList<Expense> call() throws Exception {
                List<Expense> expenseList = new ArrayList<>();
                try {
                    expensesData = FXCollections.observableArrayList();
                    preparedStatement = connection.prepareStatement("SELECT * FROM expenses WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        Expense expense = new Expense();
                        expense.setExpenseId(resultSet.getLong("expense_id"));
                        expense.setExpenseDate(resultSet.getDate("expense_date"));
                        expense.setAmount(resultSet.getBigDecimal("amount"));
                        expense.setExpenseType((Expense.ExpenseType.valueOf(resultSet.getString("expense_type"))));
                        expense.setDescription(resultSet.getString("description"));
                        expenseList.add(expense);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + ": at line 176");

                }
                return FXCollections.observableArrayList(expenseList);
            }
        };
        loadExpenseTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadExpenseTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        loadExpenseTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                expensesData = loadExpenseTask.getValue();
                expensesTable.setItems(expensesData);
            });
        });
        new Thread(loadExpenseTask).start();
    }

    @FXML
    void saveExpense(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
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

        if (!RegexValidator.isValidAmount(amountField.getText())) {
            showAlert("مبلغ نادرست", "please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal amount = new BigDecimal(amountField.getText());
        String expenseType = expenseTypeComboBox.getValue();
        String description = descriptionField.getText();
        String cardNumber = bankAccountListCombo.getValue().toString();
        Long bai = null; // bank account id
        for (BankAccount ba : bankAccounts) {
            if (cardNumber.equals(ba.getCardNumber())) {
                bai = ba.getId();
                break;
            }
        }
        Long bank_account_id = bai;
        switch (expenseType) {
            case "تعمیرات":
                expenseType = "REPAIRS";
                break;
            case "پرداخت قبوض":
                expenseType = "BILLS";
                break;
            case "دیگر":
                expenseType = "OTHER";
                break;
        }
        String finalType = expenseType;
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
                    addTransaction.setString(4, "expense");
                    addTransaction.setBigDecimal(5, amount);
                    int rowsInserted = addTransaction.executeUpdate();
                    if (rowsInserted > 0) {
                        try (ResultSet generatedKeys = addTransaction.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                transactionCode = generatedKeys.getLong(1);
                            }
                        }
                    }
                    RaziLogger.info("A transaction submited successful.");
                    // add expense
                    preparedStatement = connection.prepareStatement("INSERT INTO expenses (expense_date, amount, expense_type, description, user_id, transaction_id) VALUES (?, ?, ?, ?, ?, ?)");
                    preparedStatement.setDate(1, java.sql.Date.valueOf(gDate));
                    preparedStatement.setBigDecimal(2, amount);
                    preparedStatement.setString(3, finalType);
                    preparedStatement.setString(4, description);
                    preparedStatement.setLong(5, user_id);
                    preparedStatement.setLong(6, transactionCode);
                    preparedStatement.executeUpdate();
                    RaziLogger.info("A transaction submitted successful.");
                    // update balance
                    PreparedStatement substatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance - ? WHERE bank_account_id = ? AND user_id=?");
                    substatement.setBigDecimal(1, amount);
                    substatement.setLong(2, bank_account_id);
                    substatement.setLong(3, user_id);
                    substatement.executeUpdate();

                    // Clear fields and refresh table
                    clearFields();
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 355");
                }
                return null; // end of task
            }
        };
        saveTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = saveTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در تراکنش", "Please check your internet connection and sure about your input data.", Alert.AlertType.ERROR);
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
                loadExpenses();
                expensesTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deleteExpense(ActionEvent event) {
        if (expensesTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک هزینه را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Long transactionCode = null;
                try {
                    Expense selectedExpense = expensesTable.getSelectionModel().getSelectedItem();
                    if (selectedExpense != null) {
                        String selectSql = "SELECT transaction_id FROM expenses WHERE expense_id = ? AND user_id=?";
                        PreparedStatement foundTransaction = connection.prepareStatement(selectSql);
                        foundTransaction.setLong(1, selectedExpense.getExpenseId());
                        foundTransaction.setLong(2, user_id);
                        try (ResultSet resultSet = foundTransaction.executeQuery()) {
                            if (resultSet.next()) {
                                transactionCode = resultSet.getLong("transaction_id");
                            } else {
                                throw new SQLException("No transaction found with ID " + selectedExpense.getExpenseId());
                            }
                        }
                        preparedStatement = connection.prepareStatement("DELETE FROM expenses WHERE expense_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedExpense.getExpenseId());
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
                        expensesData.remove(selectedExpense);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 400");
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
                expensesTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        amountField.setText("");
        descriptionField.setText("");
        dayOfDateField.setText("");
        yearOfDateField.setText("");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
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

    private boolean validationForm() {
        // Validate input (e.g., check for empty fields)
        if (expenseTypeComboBox.getValue() == null || amountField.getText().isEmpty()
                || dayOfDateField.getText().isEmpty() || yearOfDateField.getText().isEmpty()
                || bankAccountListCombo.getValue() == null || bankAccountListCombo.getValue()==null) {
            return false;
        }
        return true;
    }

    @FXML
    private void clearFieledButton(ActionEvent event) {
        clearFields();
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (expensesData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM expenses WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/expenses.jrxml");
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
