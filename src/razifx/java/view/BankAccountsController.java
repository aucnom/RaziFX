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
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.BankAccount;

/**
 * BankAccountsController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class BankAccountsController implements Initializable {

    @FXML
    private TableView<BankAccount> bankAccountsTable;

    @FXML
    private TableColumn<BankAccount, Long> bankAccountIdColumn;

    @FXML
    private TableColumn<BankAccount, String> bankNameColumn;

    @FXML
    private TableColumn<BankAccount, String> accountNumberColumn;

    @FXML
    private TableColumn<BankAccount, String> cardNumberColumn;

    @FXML
    private TableColumn<BankAccount, String> accountHolderNameColumn;

    @FXML
    private TableColumn<BankAccount, String> balanceColumn;

    @FXML
    private TableColumn<BankAccount, String> descriptionColumn;

    @FXML
    private TextField bankNameField;

    @FXML
    private TextField accountNumberField;

    @FXML
    private TextField cardNumberField;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;


    @FXML
    private TextField accountHolderNameField1;

    @FXML
    private TextField balanceField1;

    @FXML
    private TextArea descriptionField1;

    @FXML
    private Button move_main;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private ObservableList<BankAccount> bankAccountsData;
    @FXML
    private Button clear_form;

    private Long user_id;
    @FXML
    private Button reportButton;
    @FXML
    private Button transactionsReportButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The bank account management stage start.");
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
        bankAccountIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bankNameColumn.setCellValueFactory(new PropertyValueFactory<>("bankName"));
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        cardNumberColumn.setCellValueFactory(new PropertyValueFactory<>("cardNumber"));
        accountHolderNameColumn.setCellValueFactory(new PropertyValueFactory<>("accountHolderName"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedBalance"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Load initial data from database
        loadBankAccounts();
        /**
         * load data from table when user double clicked on it.
         */
        bankAccountsTable.setRowFactory(tv -> {
            TableRow<BankAccount> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    BankAccount data = row.getItem();
                    bankNameField.setText(data.getBankName());
                    accountNumberField.setText(data.getAccountNumber());
                    cardNumberField.setText(data.getCardNumber());
                    accountHolderNameField1.setText(data.getAccountHolderName());
                    balanceField1.setText(data.getBalance().toString());
                    descriptionField1.setText(data.getDescription());
                }
            });
            return row;
        });
    }

    private void loadBankAccounts() {
        Task<ObservableList<BankAccount>> loadBankAccountTask = new Task<ObservableList<BankAccount>>() {
            @Override
            protected ObservableList<BankAccount> call() throws Exception {
                List<BankAccount> bankAccountList = new ArrayList<>();
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
                        bankAccountList.add(bankAccount);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + ": at line 201");

                }
                return FXCollections.observableArrayList(bankAccountList);
            }
        };
        loadBankAccountTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadBankAccountTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        loadBankAccountTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                bankAccountsData = loadBankAccountTask.getValue();
                bankAccountsTable.setItems(bankAccountsData);
            });
        });
        new Thread(loadBankAccountTask).start();
    }

    @FXML
    void saveBankAccount(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String bankName = bankNameField.getText();
        String accountNumber = accountNumberField.getText();
        String cardNumber = cardNumberField.getText();
        String accountHolderName = accountHolderNameField1.getText();
        if (!RegexValidator.isValidAmount(balanceField1.getText())) {
            showAlert("مبلغ اشتباه", "Please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal balance = new BigDecimal(balanceField1.getText());
        String description = descriptionField1.getText();
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO bank_accounts (bank_name, account_number, card_number, account_holder_name, balance, description, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, bankName);
                    preparedStatement.setString(2, accountNumber);
                    preparedStatement.setString(3, cardNumber);
                    preparedStatement.setString(4, accountHolderName);
                    preparedStatement.setBigDecimal(5, balance);
                    preparedStatement.setString(6, description);
                    preparedStatement.setLong(7, user_id);
                    preparedStatement.executeUpdate();
                    // Clear fields and refresh table
                    clearFields();
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 400");
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
                        .text("The data saved to database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                loadBankAccounts();
                bankAccountsTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void updateBankAccount(ActionEvent event) {
        if (bankAccountsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("لطفا یک حساب را انتخاب کنید", "There is no selected data from tableview to update.", Alert.AlertType.WARNING);
            return;
        }
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        BankAccount selectedBankAccount = bankAccountsTable.getSelectionModel().getSelectedItem();
        if (selectedBankAccount != null) {
            selectedBankAccount.setBankName(bankNameField.getText());
            selectedBankAccount.setAccountNumber(accountNumberField.getText());
            selectedBankAccount.setCardNumber(cardNumberField.getText());
            selectedBankAccount.setAccountHolderName(accountHolderNameField1.getText());
            if (!RegexValidator.isValidAmount(balanceField1.getText())) {
                showAlert("مبلغ اشتباه", "Please enter the correct amount", Alert.AlertType.WARNING);
                return;
            }
            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        selectedBankAccount.setBalance(new BigDecimal(balanceField1.getText()));
                        selectedBankAccount.setDescription(descriptionField1.getText());
                        preparedStatement = connection.prepareStatement("UPDATE bank_accounts SET bank_name = ?, account_number = ?, card_number = ?, account_holder_name = ?, balance = ?, description = ? WHERE bank_account_id = ? AND user_id=?");
                        preparedStatement.setString(1, selectedBankAccount.getBankName());
                        preparedStatement.setString(2, selectedBankAccount.getAccountNumber());
                        preparedStatement.setString(3, selectedBankAccount.getCardNumber());
                        preparedStatement.setString(4, selectedBankAccount.getAccountHolderName());
                        preparedStatement.setBigDecimal(5, selectedBankAccount.getBalance());
                        preparedStatement.setString(6, selectedBankAccount.getDescription());
                        preparedStatement.setLong(7, selectedBankAccount.getId());
                        preparedStatement.setLong(8, user_id);
                        preparedStatement.executeUpdate();
                        // Refresh table
                        clearFields();
                    } catch (SQLException e) {
                        throw new Exception(getClass().getName() + " at line 400");
                    }
                    return null; // end of task
                }
            };
            updateTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent arg0) {
                    Throwable throwable = updateTask.getException();
                    RaziLogger.error(throwable.getMessage());
                    showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                    return;
                }
            });
            updateTask.setOnSucceeded(s -> {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("RaziFX")
                            .text("The data updated to database successful.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(7))
                            .position(Pos.BOTTOM_RIGHT)
                            .darkStyle();
                    notifications.show();
                    loadBankAccounts();
                    bankAccountsTable.refresh();
                });
            });
            new Thread(updateTask).start();
        }
    }

    @FXML
    void transactionsReport(ActionEvent event) {
        if (bankAccountsData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        if (bankAccountsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("لطفا یک حساب را انتخاب کنید", "There is no selected bank account from tableview to report.", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            BankAccount selectedAccount = bankAccountsTable.getSelectionModel().getSelectedItem();
            Long account_id = selectedAccount.getId();
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT transaction_id, IF(transaction_type=\"payment\", \"افزایشی\", \"کاهشی\") as tran_type, transaction_date, amount FROM transactions WHERE user_id=" + user_id + " AND account_id=" + account_id + ";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/transactions.jrxml");
                JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);

                // Set Parameters (if any)
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("parameter1", selectedAccount.getAccountNumber());

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

    private void clearFields() {
        bankNameField.setText("");
        accountNumberField.setText("");
        cardNumberField.setText("");
        accountHolderNameField1.setText("");
        balanceField1.setText("");
        descriptionField1.setText("");
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
        if (bankNameField.getText().isEmpty() || accountNumberField.getText().isEmpty()
                || cardNumberField.getText().isEmpty() || accountHolderNameField1.getText().isEmpty()
                || balanceField1.getText().isEmpty()) {
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
        if (bankAccountsData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM bank_accounts WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/bank_accounts.jrxml");
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
