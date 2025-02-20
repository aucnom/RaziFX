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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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
import org.controlsfx.control.Notifications;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Customer;
import razifx.java.model.entity.Order;

/**
 * OrderingController.java Customer -> Order -> OrderDetail -> Items
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class OrderingController implements Initializable {

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, String> customerIdColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TextField customerIdField;

    @FXML
    private ComboBox<String> statusComboBox;

    private ObservableList<Order> ordersData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @FXML
    private Button move_main;

    private Long user_id;

    @FXML
    private ComboBox<String> customerNameComboBox;

    @FXML
    private Button selectCustomerButton;

    @FXML
    private TextField dayOfDateField;

    @FXML
    private ComboBox<String> mounthOfDateField;

    @FXML
    private TextField yearOfDateField;

    @FXML
    private Button clear_form;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;


    @FXML
    private Button createInvoiceButton;

    private ArrayList<Customer> customerList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The order stage start.");
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
        loadCustomers();

        // Initialize table columns
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCustomerName"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedOrderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("formattedStatus"));

        // Initialize expenseTypeComboBox
        // "در انتظار"، "در حال پردازش"، "ارسال شده"، "تحویل شده"، "لغو"
        statusComboBox.getItems().removeAll(statusComboBox.getItems());
        statusComboBox.getItems().addAll("در انتظار", "در حال پردازش", "ارسال شده", "تحویل شده", "لغو");
        statusComboBox.getSelectionModel().select("در انتظار");

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        customerNameComboBox.getItems().removeAll(customerNameComboBox.getItems());
        customerList.forEach(c -> {
            customerNameComboBox.getItems().add(c.getFullName());
        });

    }

    private void loadCustomers() {
        customerList = new ArrayList<>();
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
        } catch (SQLException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            return;
        }
    }

    @FXML
    void saveOrder(ActionEvent event
    ) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill the all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }

        Long customerId = Long.parseLong(customerIdField.getText());
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

        // "در انتظار"، "در حال پردازش"، "ارسال شده"، "تحویل شده"، "لغو"
        String status = statusComboBox.getValue();
        switch (status) {
            case "در انتظار":
                status = "PENDING";
                break;
            case "در حال پردازش":
                status = "PROCESSING";
                break;
            case "ارسال شده":
                status = "SHIPPED";
                break;
            case "تحویل شده":
                status = "DELIVERED";
                break;
            case "لغو":
                status = "CANCELLED";
                break;
        }
        String finalStatus = status;
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO orders (customer_id ,order_date ,status, user_id) VALUES (?, ?, ?, ?)");
                    preparedStatement.setLong(1, customerId);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(gDate));
                    preparedStatement.setString(3, finalStatus);
                    preparedStatement.setLong(4, user_id);
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
                        .text("The data submitted to database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                loadOrders(customerId);
                ordersTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void updateOrderStatus(ActionEvent event) {
        if (ordersTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("لطفا یک سفارش را انتخاب کنید", "There is no selected data from tableview to update.", Alert.AlertType.WARNING);
            return;
        }
        if (statusComboBox.getValue() == null) {
            showAlert("وضعیت را مشخص کنید", "Please select correct status.", Alert.AlertType.WARNING);
            return;
        }
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            Long customerId = Long.parseLong(customerIdField.getText());
            String status = statusComboBox.getValue();
            switch (status) {
                case "در انتظار":
                    status = "PENDING";
                    break;
                case "در حال پردازش":
                    status = "PROCESSING";
                    break;
                case "ارسال شده":
                    status = "SHIPPED";
                    break;
                case "تحویل شده":
                    status = "DELIVERED";
                    break;
                case "لغو":
                    status = "CANCELLED";
                    break;
            }
            String finalStatus = status;
            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        selectedOrder.setStatus(Order.OrderStatus.valueOf(finalStatus));
                        preparedStatement = connection.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ? AND user_id=?");
                        preparedStatement.setString(1, selectedOrder.getStatus().toString());
                        preparedStatement.setLong(2, selectedOrder.getOrderId());
                        preparedStatement.setLong(3, user_id);
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
                            .title("RaziFX ")
                            .text("The data updated to database successful.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(7))
                            .position(Pos.BOTTOM_RIGHT)
                            .darkStyle();
                    notifications.show();
                    loadOrders(customerId);
                    ordersTable.refresh();
                });
            });
            new Thread(updateTask).start();
        }
    }

    @FXML
    void deleteOrder(ActionEvent event) {
        if (ordersTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک سفارش را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        // first delete all refrences created by order_id
                        preparedStatement = connection.prepareStatement("DELETE FROM payments WHERE order_id = ?");
                        preparedStatement.setLong(1, selectedOrder.getOrderId());
                        preparedStatement.executeUpdate();

                        preparedStatement = connection.prepareStatement("DELETE FROM order_details WHERE order_id = ?");
                        preparedStatement.setLong(1, selectedOrder.getOrderId());
                        preparedStatement.executeUpdate();

                        preparedStatement = connection.prepareStatement("DELETE FROM orders WHERE order_id = ?");
                        preparedStatement.setLong(1, selectedOrder.getOrderId());
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        ordersData.remove(selectedOrder);
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
                ordersTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private boolean validationForm() {
        if (dayOfDateField.getText().isEmpty() || yearOfDateField.getText().isEmpty()
                || mounthOfDateField.getValue() == null || customerNameComboBox.getValue() == null
                || customerIdField.getText().isEmpty()) {
            return false;
        }
        return true;
    }

    private void clearFields() {
        dayOfDateField.setText("");
        yearOfDateField.setText("");
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void handleSelectCustomer(ActionEvent event) {
        if (customerNameComboBox.getValue()==null) {
            showAlert("مشتری را انتخاب کنید", "Please select a customer to create or load an order.", Alert.AlertType.WARNING);
            return;
        }
        String customerFullName = customerNameComboBox.getValue().toString();
        Customer customer = null;
        Long customerId = null;
        for (Customer c : customerList) {
            if (customerFullName.equals(c.getFullName())) {
                customer = c;
                customerId = c.getCustomerId();
                customerIdField.setText(customerId.toString());
                break;
            }
        }
        loadOrdersWithCustomerId(customerId, customer);
    }

    private void loadOrders(Long customer_id) {
        Platform.runLater(() -> {
            Customer customer = null;
            for (Customer c : customerList) {
                if (customer_id == c.getCustomerId()) {
                    customer = c;
                    break;
                }
            }
            loadOrdersWithCustomerId(customer_id, customer);
        });
    }

    private void loadOrdersWithCustomerId(Long customer_id, Customer customer) {
        Long customerID = customer_id;
        Task<ObservableList<Order>> loadOrderById = new Task<ObservableList<Order>>() {
            @Override
            protected ObservableList<Order> call() throws Exception {
                List<Order> order_list = new ArrayList<>();
                try {
                    ordersData = FXCollections.observableArrayList();
                    preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE customer_id = ? AND user_id=?");
                    preparedStatement.setLong(1, customerID);
                    preparedStatement.setLong(2, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        Order order = new Order();
                        order.setOrderId(resultSet.getLong("order_id"));
                        order.setCustomer(customer);
                        order.setOrderDate(resultSet.getDate("order_date"));
                        order.setStatus(Order.OrderStatus.valueOf(resultSet.getString("status")));
                        order_list.add(order);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + ": at line 379");

                }
                return FXCollections.observableArrayList(order_list);
            }
        };
        loadOrderById.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadOrderById.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            }
        });
        loadOrderById.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                ordersData = loadOrderById.getValue();
                ordersTable.setItems(ordersData);
            });
        });
        new Thread(loadOrderById).start();
    }

    @FXML
    private void clearFieledButton(ActionEvent event) {
        clearFields();
    }

    @FXML
    private void createInvoice(ActionEvent event) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                Order sendOrder = new Order();
                sendOrder.setCustomer(selectedOrder.getCustomer());
                sendOrder.setOrderDate(selectedOrder.getOrderDate());
                sendOrder.setStatus(selectedOrder.getStatus());
                sendOrder.setOrderId(selectedOrder.getOrderId());
                
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("razifx/resources/fxml/order_registration.fxml"));
                Parent root = (Parent) loader.load();
                OrderRegistrationController odc = loader.getController();
                odc.setOrder(sendOrder);
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("فاکتور فروش");
                stage.setResizable(false);
            } catch (IOException ex) {
                RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            }
        } else {
            showAlert("لطفا یک سفارش را انخاب کنید", "Please select one order in the table.", Alert.AlertType.WARNING);
            return;
        }
    }
}
