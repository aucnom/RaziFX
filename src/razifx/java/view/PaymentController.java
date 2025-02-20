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
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.BankAccount;
import razifx.java.model.entity.Customer;
import razifx.java.model.entity.Order;
import razifx.java.model.entity.OrderDetail;
import razifx.java.model.entity.Payment;
import razifx.java.model.entity.Product;

/**
 * FXML Controller class razifx.java.view.PaymentController
 * PaymentController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class PaymentController implements Initializable {

    @FXML
    private TableView<Payment> paymentsTable;

    @FXML
    private TableColumn<Payment, Long> orderIdColumn;

    @FXML
    private TableColumn<Payment, String> paymentDateColumn;

    @FXML
    private TableColumn<Payment, String> amountColumn;

    @FXML
    private TableColumn<Payment, String> paymentMethodColumn;

    @FXML
    private Button search_btn;

    @FXML
    private TableColumn<Payment, String> discountColumn;

    @FXML
    private TextField discountTextField;

    @FXML
    private ComboBox<String> BankAccountComboBox;

    @FXML
    private ComboBox<Long> orderIdField;

    @FXML
    private TextField amountField;

    @FXML
    private Button move_main;

    @FXML
    private Button savePaymentButton;

    @FXML
    private Button deletePaymentButton;

    private ObservableList<Payment> paymentsData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private List<Customer> customerList;
    private List<Order> orderList;
    private List<OrderDetail> orderDetailList;
    private List<Product> productList;
    private ArrayList<BankAccount> bankAccountList;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    private Long user_id;

    @FXML
    private TextField order_customer_name;

    @FXML
    private TextField order_date;

    @FXML
    private TextField order_status;
    @FXML
    private Button reportButton;
    @FXML
    private TextField orderIdTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The payment stage start.");
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
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedOrderID"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPaymentDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPaymentMethod"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDiscount"));
        // 1. Load customers
        customerList = new ArrayList<>();
        loadCustomers();
        // 2. Load Products
        productList = new ArrayList<>();
        loadProducts();
        // 3. Load Orders
        orderList = new ArrayList<>();
        loadOrders();
        // 4. Load Order details
        orderDetailList = new ArrayList<>();
        loadOrderDetails();
        // 5. Load bank acoounts
        loadBankAccount();
        BankAccountComboBox.getItems().removeAll(BankAccountComboBox.getItems());
        if (bankAccountList != null) {
            bankAccountList.stream().forEach(ba -> {
                BankAccountComboBox.getItems().add(ba.getCardNumber());
            });
        } else {
            showAlert("ابتدا حساب بانکی تعریف کنید", "Please intial a bank account first.", Alert.AlertType.INFORMATION);
            return;
        }
        // Initialize expenseTypeComboBox
        // کارت اعتباری، کارت نقدی، نقدی، حواله بانکی
        paymentMethodComboBox.getItems().removeAll(paymentMethodComboBox.getItems());
        paymentMethodComboBox.getItems().addAll("حواله بانکی", "نقدی", "کارت خوان", "چک");
        paymentMethodComboBox.getSelectionModel().select("نقدی");
        orderIdField.getItems().removeAll(orderIdField.getItems());
        orderList.stream().forEach(o -> {
            orderIdField.getItems().add(o.getOrderId());
        });
        // Load payments
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

    private void loadProducts() {
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM products WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(resultSet.getLong("product_id"));
                product.setName(resultSet.getString("name"));
                product.setPrice(resultSet.getBigDecimal("price"));
                product.setDescription(resultSet.getString("description"));
                productList.add(product);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadCustomers() {
        try {
            preparedStatement = connection.prepareStatement("SELECT customer_id, first_name, last_name, national_id, birthdate, phone_number, address, gender From customers WHERE user_id=?");
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
                customerList.add(customer);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadOrderDetails() {
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM order_details WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                OrderDetail orderDetail = new OrderDetail();
                Long order_detail_id = resultSet.getLong("order_detail_id");
                Long order_id = resultSet.getLong("order_id");
                Long product_id = resultSet.getLong("product_id");
                Integer quantity = resultSet.getInt("quantity");
                BigDecimal unit_price = resultSet.getBigDecimal("unit_price");
                Order o = null;
                Product p = null;
                for (Order order : orderList) {
                    if (order_id == order.getOrderId()) {
                        o = order;
                    }
                }
                for (Product product : productList) {
                    if (product_id == product.getProductId()) {
                        p = product;
                    }
                }
                orderDetail.setOrderDetailId(order_detail_id);
                orderDetail.setOrder(o);
                orderDetail.setProduct(p);
                orderDetail.setQuantity(quantity);
                orderDetail.setUnitPrice(unit_price);
                orderDetailList.add(orderDetail);
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadPayments() {
        Task<ObservableList<Payment>> loadTask = new Task<ObservableList<Payment>>() {
            @Override
            protected ObservableList<Payment> call() throws Exception {
                ArrayList<Payment> payments = new ArrayList<>();
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM jobs WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        paymentsData = FXCollections.observableArrayList();
                        preparedStatement = connection.prepareStatement("SELECT * FROM payments WHERE user_id=?");
                        preparedStatement.setLong(1, user_id);
                        resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            Payment payment = new Payment();
                            payment.setPaymentId(resultSet.getLong("payment_id"));
                            Long order_id = resultSet.getLong("order_id");
                            Order order = null;
                            for (Order o : orderList) {
                                if (order_id == o.getOrderId()) {
                                    if (!o.getStatus().equals(Order.OrderStatus.CANCELLED)) {
                                        order = o;
                                    }
                                }
                            }
                            payment.setOrder(order);
                            payment.setPaymentDate(resultSet.getDate("payment_date"));
                            payment.setAmount(resultSet.getBigDecimal("amount"));
                            payment.setDiscount(resultSet.getBigDecimal("discount"));
                            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(resultSet.getString("payment_method")));
                            payments.add(payment);
                        }
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 249");
                }
                return FXCollections.observableArrayList(payments);
            }
        };
        loadTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                paymentsData = loadTask.getValue();
                paymentsTable.setItems(paymentsData);
            });
        });
        new Thread(loadTask).start();
    }

    private void loadOrders() {
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong("order_id"));
                Long customer_id = resultSet.getLong("customer_id");
                Customer c = null;
                for (Customer customer : customerList) {
                    if (customer_id == customer.getCustomerId()) {
                        c = customer;
                    }
                }
                order.setCustomer(c);
                order.setOrderDate(resultSet.getDate("order_date"));
                order.setStatus(Order.OrderStatus.valueOf(resultSet.getString("status")));
                order.setOrderDetails(orderDetailList);
                orderList.add(order);
            }
        } catch (SQLException e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    @FXML
    void savePayment(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        if (!RegexValidator.isNumeric(orderIdTextField.getText())) {
            showAlert("شناسه نادرست", "Please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        Long orderId = Long.parseLong(orderIdField.getValue().toString());
        if (!paymentsData.isEmpty()) {
            for (Payment p : paymentsData) {
                if (orderId == p.getOrder().getOrderId()) {
                    showAlert("سفارش انتخاب شده پرداخت شده است", "The selected order has already been paid.", Alert.AlertType.INFORMATION);
                    return;
                }
            }
        }
        if (!RegexValidator.isValidAmount(amountField.getText())) {
            showAlert("مبلغ نادرست", "Please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        if (!RegexValidator.isValidAmount(discountTextField.getText())) {
            showAlert("مبلغ نادرست", "Please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal amount = new BigDecimal(amountField.getText());
        BigDecimal discount = new BigDecimal(discountTextField.getText());
        BigDecimal total_payment = amount.subtract(discount);
        String paymentMethod = paymentMethodComboBox.getValue().toString();

        String cardNumber = BankAccountComboBox.getValue().toString();
        Long bai = null; // bank account id
        for (BankAccount ba : bankAccountList) {
            if (cardNumber.equals(ba.getCardNumber())) {
                bai = ba.getId();
                break;
            }
        }
        Long bank_account_id = bai;
        // کارت اعتباری، کارت نقدی، نقدی، حواله بانکی
        switch (paymentMethod) {
            case "حواله بانکی":
                paymentMethod = "BANK_TRANSFER";
                break;
            case "کارت خوان":
                paymentMethod = "DEBIT_CARD";
                break;
            case "نقدی":
                paymentMethod = "CASH";
                break;
            case "چک":
                paymentMethod = "CREDIT_CARD";
                break;
        }
        String pm = paymentMethod;
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
                    addTransaction.setString(4, "payment");
                    addTransaction.setBigDecimal(5, total_payment);
                    int rowsInserted = addTransaction.executeUpdate();
                    if (rowsInserted > 0) {
                        try (ResultSet generatedKeys = addTransaction.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                transactionCode = generatedKeys.getLong(1);
                            }
                        }
                    }
                    RaziLogger.info("A transaction submitted successful.");
                    preparedStatement = connection.prepareStatement("INSERT INTO payments (order_id, payment_date, amount, payment_method,discount,  user_id, transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setLong(1, orderId);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(paymentDate));
                    preparedStatement.setBigDecimal(3, amount);
                    preparedStatement.setString(4, pm);
                    preparedStatement.setBigDecimal(5, discount);
                    preparedStatement.setLong(6, user_id);
                    preparedStatement.setLong(7, transactionCode);
                    preparedStatement.executeUpdate();

                    PreparedStatement addStatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance + ? WHERE bank_account_id = ? AND user_id=?");
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
            showAlert("یک پرداخت را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Long transactionCode = null;
                try {
                    Payment selectedPayment = paymentsTable.getSelectionModel().getSelectedItem();
                    if (selectedPayment != null) {
                        String selectSql = "SELECT transaction_id FROM payments WHERE payment_id = ? AND user_id=?";
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

                        preparedStatement = connection.prepareStatement("DELETE FROM payments WHERE order_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedPayment.getOrder().getOrderId());
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
                        RaziLogger.info("The transaction with id " + transactionCode + " deleted successful.");
                        // update balance
                        PreparedStatement subStatement = connection.prepareStatement("UPDATE bank_accounts SET balance = balance - ? WHERE bank_account_id = ? AND user_id=?");
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
        discountTextField.setText("0");
    }

    private boolean validationForm() {
        if (amountField.getText().isEmpty() || discountTextField.getText().isEmpty() || BankAccountComboBox.getValue() == null
                || orderIdTextField.getText().isEmpty()) {
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void searchOrder(ActionEvent event) {
        if (orderIdField.getValue() == null) {
            showAlert("لطفا سفارشی را انتخاب کند", "Please select an order first.", Alert.AlertType.WARNING);
            return;
        }

        //  Load initial Payments data from database
        Long orderId = Long.parseLong(orderIdField.getValue().toString());
        BigDecimal totalPrice = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT SUM(quantity * unit_price) AS total_price FROM order_details WHERE order_id = ? AND user_id=?");
            preparedStatement.setLong(1, orderId);
            preparedStatement.setLong(2, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                totalPrice = resultSet.getBigDecimal("total_price");
            }
            if (totalPrice == null || totalPrice.doubleValue() <= 0) {
                showAlert("محصولی خریداری نشده است.", "The order has no items", Alert.AlertType.INFORMATION);
                return;
            }
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا", "Error to sum total price of the order.", Alert.AlertType.ERROR);
        }
        amountField.setText(totalPrice.toString());
        for (Order o : orderList) {
            if (orderId == o.getOrderId()) {
                order_customer_name.setText(o.getFormattedCustomerName());
                order_date.setText(o.getFormattedOrderDate());
                orderIdTextField.setText(o.getCustomer().getCustomerId().toString());
                order_status.setText(o.getFormattedStatus());
            }
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
                        + "    p.payment_id,\n"
                        + "    o.order_id,\n"
                        + "    c.customer_id,\n"
                        + "    c.first_name,\n"
                        + "    c.last_name,\n"
                        + "    p.payment_date,\n"
                        + "    p.amount,\n"
                        + "    p.discount,\n"
                        + "    p.payment_method,\n"
                        + "    o.status AS order_status  -- Include order status for context\n"
                        + "FROM\n"
                        + "    payments AS p \n "
                        + "JOIN\n"
                        + "    orders AS o ON p.order_id = o.order_id\n"
                        + "JOIN\n"
                        + "    customers AS c ON o.customer_id = c.customer_id\n"
                        + "WHERE p.user_id = " + user_id + " AND o.user_id = " + user_id + " AND c.user_id = " + user_id + " \n"
                        + "ORDER BY\n"
                        + "    p.payment_date DESC;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/payments.jrxml");
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
                RaziLogger.error("An error occuredduring reporting.");
                showAlert("خطا", "Failed to Obtain data from db.", Alert.AlertType.ERROR);
            }
        });
    }
}
