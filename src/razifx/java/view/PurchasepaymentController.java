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
import razifx.java.model.entity.PurchasePayment;
import razifx.java.model.entity.Supplier;

/**
 * FXML Controller class razifx.java.view.PurchasepaymentController
 * PurchasepaymentController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class PurchasepaymentController implements Initializable {

    @FXML
    private TableView<PurchasePayment> paymentsTable;

    @FXML
    private TableColumn<PurchasePayment, Long> paymentIdColumn;

    @FXML
    private TableColumn<PurchasePayment, String> supplierIdColumn;

    @FXML
    private TableColumn<PurchasePayment, String> paymentDateColumn;

    @FXML
    private TableColumn<PurchasePayment, String> amountColumn;

    @FXML
    private TableColumn<PurchasePayment, String> paymentMethodColumn;

    @FXML
    private TableColumn<PurchasePayment, String> discountColumn;

    @FXML
    private TextField discountTestField;

    @FXML
    private ComboBox<String> bankAccountComboBox;

    @FXML
    private ComboBox<String> supplierIdField;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private ObservableList<PurchasePayment> paymentsData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;
    private ArrayList<Supplier> suppliers;

    @FXML
    private Button move_main;
    @FXML
    private Button reportButton;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;

    ArrayList<BankAccount> bankAccountList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The purchase stage start.");
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
        paymentIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedSupplierFullName"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPaymentDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPaymentMethod"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDiscount"));
        // Load all suppliers
        supplierIdField.getItems().removeAll(supplierIdField.getItems());
        loadSuppliers();
        // چک، نقدی، حواله بانکی
        paymentMethodComboBox.getItems().removeAll(paymentMethodComboBox.getItems());
        paymentMethodComboBox.getItems().addAll("حواله بانکی", "نقدی", "چک");
        paymentMethodComboBox.getSelectionModel().select("نقدی");

        loadBankAccount();
        bankAccountComboBox.getItems().removeAll(bankAccountComboBox.getItems());
        if (bankAccountList != null) {
            bankAccountList.stream().forEach(ba -> {
                bankAccountComboBox.getItems().add(ba.getCardNumber());
            });
        } else {
            showAlert("ابتدا حساب بانکی تعریف کنید", "Please intial a bank account first.", Alert.AlertType.INFORMATION);
            return;
        }
        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        loadPayments();

    }

    private void loadBankAccount() {
        bankAccountList = new ArrayList<>();
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
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    private void loadSuppliers() {
        suppliers = new ArrayList<>();

        try {
            preparedStatement = connection.prepareStatement("SELECT supplier_id, first_name, last_name, national_id, birthdate, phone_number, address, gender From suppliers WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(resultSet.getLong("supplier_id"));
                supplier.setFirstName(resultSet.getString("first_name"));
                supplier.setLastName(resultSet.getString("last_name"));
                supplier.setNationalId(resultSet.getString("national_id"));
                supplier.setBirthdate(resultSet.getDate("birthdate"));
                supplier.setPhoneNumber(resultSet.getString("phone_number"));
                supplier.setAddress(resultSet.getString("address"));
                supplier.setGender(Supplier.Gender.valueOf(resultSet.getString("gender")));
                suppliers.add(supplier);
                supplierIdField.getItems().add(supplier.getFullName());
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadPayments() {
        try {
            paymentsData = FXCollections.observableArrayList();
            preparedStatement = connection.prepareStatement("SELECT * FROM purchase_payments WHERE user_id=? ");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PurchasePayment payment = new PurchasePayment();
                payment.setPaymentId(resultSet.getLong("payment_id"));
                Long suplier_id = resultSet.getLong("supplier_id");
                Supplier supplier = null;
                for (Supplier s : suppliers) {
                    if (suplier_id == s.getSupplierId()) {
                        supplier = s;
                        break;
                    }
                }
                payment.setSupplier(supplier);
                payment.setPaymentDate(resultSet.getDate("payment_date"));
                payment.setAmount(resultSet.getBigDecimal("amount"));
                payment.setDiscount(resultSet.getBigDecimal("discount"));
                payment.setPaymentMethod(PurchasePayment.PaymentMethod.valueOf(resultSet.getString("payment_method")));
                paymentsData.add(payment);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا", "Error to load the purchase payment data from database.", Alert.AlertType.ERROR);
            return;
        }
        paymentsTable.setItems(paymentsData);
    }

    @FXML
    void savePayment(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }

        Long supplierId = null;
        String supplierFullName = supplierIdField.getValue().toString();
        for (Supplier s : suppliers) {
            if (supplierFullName.equals(s.getFullName())) {
                supplierId = s.getSupplierId();
                break;
            }
        }
        Long finalSupplierId = supplierId;
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
            showAlert("مبلغ نادرست", "Please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal amount = new BigDecimal(amountField.getText());
        BigDecimal discount = new BigDecimal(discountTestField.getText());
        BigDecimal total_payment = amount.subtract(discount);
        String paymentMethod = paymentMethodComboBox.getValue();

        // کارت اعتباری، نقدی، حواله بانکی
        switch (paymentMethod) {
            case "حواله بانکی":
                paymentMethod = "BANK_TRANSFER";
                break;
            case "نقدی":
                paymentMethod = "CASH";
                break;
            case "چک":
                paymentMethod = "CHECK";
                break;
        }
        String pm = paymentMethod;

        String cardNumber = bankAccountComboBox.getValue().toString();
        Long bai = null; // bank account id
        for (BankAccount ba : bankAccountList) {
            if (cardNumber.equals(ba.getCardNumber())) {
                bai = ba.getId();
                break;
            }
        }
        Long bank_account_id = bai;
        // Get current date for payment date
        LocalDate paymentDate = LocalDate.now();
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
                    addTransaction.setDate(3, Date.valueOf(LocalDate.now()));
                    addTransaction.setString(4, "purchase_payment");
                    addTransaction.setBigDecimal(5, total_payment);
                    int rowsInserted = addTransaction.executeUpdate();
                    if (rowsInserted > 0) {
                        try (ResultSet generatedKeys = addTransaction.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                transactionCode = generatedKeys.getLong(1);
                            }
                        }
                    }

                    preparedStatement = connection.prepareStatement("INSERT INTO purchase_payments (supplier_id, payment_date, amount, discount, payment_method, user_id, transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setLong(1, finalSupplierId);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(gDate));
                    preparedStatement.setBigDecimal(3, amount);
                    preparedStatement.setBigDecimal(4, discount);
                    preparedStatement.setString(5, pm);
                    preparedStatement.setLong(6, user_id);
                    preparedStatement.setLong(7, transactionCode);
                    preparedStatement.executeUpdate();
                    // Clear fields and refresh table
                    clearFields();

                    PreparedStatement addStatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance - ? WHERE bank_account_id = ? AND user_id=?");
                    addStatement.setBigDecimal(1, total_payment);
                    addStatement.setLong(2, bank_account_id);
                    addStatement.setLong(3, user_id);
                    addStatement.executeUpdate();

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
                loadPayments();
                paymentsTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deletePayment(ActionEvent event) {
        if (paymentsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک خرید را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Long transactionCode = null;
                try {
                    PurchasePayment selectedPayment = paymentsTable.getSelectionModel().getSelectedItem();
                    if (selectedPayment != null) {
                        String selectSql = "SELECT transaction_id FROM purchase_payments WHERE payment_id = ? AND user_id=?";
                        PreparedStatement foundTransaction = connection.prepareStatement(selectSql);
                        foundTransaction.setLong(1, selectedPayment.getPaymentId());
                        foundTransaction.setLong(2, user_id);
                        try (ResultSet resultSet = foundTransaction.executeQuery()) {
                            if (resultSet.next()) {
                                transactionCode = resultSet.getLong("transaction_id");
                            } else {
                                throw new SQLException("No transaction found with ID " + selectedPayment.getPaymentId());
                            }
                        }

                        preparedStatement = connection.prepareStatement("DELETE FROM purchase_payments WHERE payment_id = ? AND user_id=? ");
                        preparedStatement.setLong(1, selectedPayment.getPaymentId());
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

                        // delete transaction
                        PreparedStatement delTransaction = connection.prepareStatement("DELETE FROM transactions WHERE transaction_id = ? AND user_id=?");
                        delTransaction.setLong(1, transactionCode);
                        delTransaction.setLong(2, user_id);
                        delTransaction.executeUpdate();

                        // update balance
                        PreparedStatement subStatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance + ? WHERE bank_account_id = ? AND user_id=?");
                        subStatement.setBigDecimal(1, amount);
                        subStatement.setLong(2, bank_account_id);
                        subStatement.setLong(3, user_id);
                        subStatement.executeUpdate();

                        // Remove from table and refresh
                        paymentsData.remove(selectedPayment);
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
                paymentsTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        amountField.setText("");
        discountTestField.setText("0");
        dayOfDateField.setText("01");
        yearOfDateField.setText("1403");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private boolean validationForm() {
        if (amountField.getText().isEmpty() || dayOfDateField.getText().isEmpty()
                || yearOfDateField.getText().isEmpty() || discountTestField.getText().isEmpty()
                || paymentMethodComboBox.getValue() == null || supplierIdField.getValue() == null) {
            return false;
        }
        return true;
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
    private void printReport(ActionEvent event) {
        if (paymentsData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT\n"
                        + "    pp.payment_id,\n"
                        + "    s.first_name,\n"
                        + "    s.last_name,\n"
                        + "    pp.payment_date,\n"
                        + "    pp.amount,\n"
                        + "    pp.discount,\n"
                        + "    pp.payment_method\n"
                        + "FROM\n"
                        + "    purchase_payments AS pp\n"
                        + "INNER JOIN\n"
                        + "    suppliers AS s ON pp.supplier_id = s.supplier_id\n"
                        + "WHERE\n"
                        + "    pp.user_id = " + user_id + " AND s.user_id = " + user_id + " \n"
                        + "ORDER BY\n"
                        + "    pp.payment_date DESC;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/purchase_payments.jrxml");
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
