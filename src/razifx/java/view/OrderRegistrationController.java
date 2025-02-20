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
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Customer;
import razifx.java.model.entity.Order;
import razifx.java.model.entity.OrderDetail;
import razifx.java.model.entity.Product;

/**
 * FXML Controller class OrderRegistrationController.java
 *
 * @author mahdihoseinzade
 * @since 1.0.1
 */
public class OrderRegistrationController implements Initializable {

    @FXML
    private TableView<OrderDetail> orderDetailsTable;

    @FXML
    private TableColumn<OrderDetail, Long> orderIdColumn;

    @FXML
    private TableColumn<OrderDetail, String> productIdColumn;

    @FXML
    private TableColumn<OrderDetail, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderDetail, String> unitPriceColumn;

    @FXML
    private TextField productIdTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Button move_main;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;

    private Order order;
    private Customer customer;

    private ArrayList<Product> productList;

    @FXML
    private TextField customerIdTextField;

    @FXML
    private TextField firstNameCustomerTextField;

    @FXML
    private TextField lastNameCustomerTextField;

    @FXML
    private TextField nationalIdCustomerTextField;

    @FXML
    private TextField phoneNumberCustomerTextField;

    @FXML
    private TextField orderIdTextField;

    @FXML
    private TextField orderDateTextField;

    @FXML
    private ComboBox<String> productNameComboBox;

    private ObservableList<OrderDetail> orderDetailsData;

    @FXML
    private Button selectProductButton;

    @FXML
    private TextField quantityTextField;

    @FXML
    private TextField unitPriceTextField;

    @FXML
    private Button addProductToTableButton;

    @FXML
    private Button removeProductFromTableButton;

    private Long order_id;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The order - order registration stage start.");
        db = new DBConnector();
        connection = db.connect();

        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }

        // initial nessecaries data
        loadProducts();
        productNameComboBox.getItems().removeAll(productNameComboBox.getItems());
        productList.forEach(c -> {
            productNameComboBox.getItems().add(c.getName());
        });

        // Initialize table columns
        loadOrderDetails();
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedProductName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedUnitPrice"));

        try {
            connection.setAutoCommit(false); // Start the transaction, Disable auto-commit
        } catch (SQLException ex) {
            Logger.getLogger(OrderRegistrationController.class.getName()).log(Level.SEVERE, null, ex);
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
        }
    }

    private void loadOrderDetails() {
        if (order != null) {
            try {
                orderDetailsData = FXCollections.observableArrayList();
                preparedStatement = connection.prepareStatement("SELECT * FROM order_details WHERE order_id = ? AND user_id=?");
                preparedStatement.setLong(1, order_id);
                preparedStatement.setLong(2, user_id);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    Long productID = resultSet.getLong("product_id");
                    Product selectedProduct = null;
                    for (Product p : productList) {
                        if (productID == p.getProductId()) {
                            selectedProduct = p;
                        }
                    }
                    orderDetail.setProduct(selectedProduct);
                    orderDetail.setQuantity(resultSet.getInt("quantity"));
                    orderDetail.setUnitPrice(resultSet.getBigDecimal("unit_price"));
                    orderDetailsData.add(orderDetail);
                }
                orderDetailsTable.setItems(orderDetailsData);
            } catch (SQLException e) {
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        }
    }

    void setOrder(Order order) {
        if (order != null) {
            this.order = order;
            this.order_id = order.getOrderId();
            this.customer = order.getCustomer();
            customerIdTextField.setText(customer.getCustomerId().toString());
            firstNameCustomerTextField.setText(customer.getFirstName());
            lastNameCustomerTextField.setText(customer.getLastName());
            nationalIdCustomerTextField.setText(customer.getNationalId());
            phoneNumberCustomerTextField.setText(customer.getPhoneNumber());
            orderIdTextField.setText(order.getOrderId().toString());
            orderDateTextField.setText(order.getFormattedOrderDate());
            loadProducts();
            loadOrderDetails();
        }
    }

    private void clearFields() {
        quantityTextField.setText("");

    }

    private void loadProducts() {
        productList = new ArrayList<>();
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

    @FXML
    private void handleSelectProduct(ActionEvent event) {
        if (productNameComboBox.getValue() == null) {
            showAlert("یک محصول را انتخاب کنید", "Please select a product to order.", Alert.AlertType.WARNING);
            return;
        }
        String productName = productNameComboBox.getValue().toString();
        for (Product p : productList) {
            if (productName.equals(p.getName())) {
                productIdTextField.setText(p.getProductId().toString());
                unitPriceTextField.setText(p.getFormattedPrice());
            }
        }
    }

    @FXML
    private void handleAddProductToTable(ActionEvent event) {
        if (unitPriceTextField.getText().isEmpty()) {
            showAlert("ابتدا محصولی را انتخاب کنید", "Please select a product first.", Alert.AlertType.WARNING);
            return;
        }
        if (quantityTextField.getText().isEmpty()) {
            showAlert("تعداد را وارد کنید", "Please enter the quantity.", Alert.AlertType.WARNING);
            return;
        }
        Long orderId = order.getOrderId();
        Long productId = Long.valueOf(productIdTextField.getText());
        Integer quantity = Integer.valueOf(quantityTextField.getText());
        BigDecimal unitPrice = null;
        for (Product p : productList) {
            if (productId == p.getProductId()) {
                unitPrice = p.getPrice();
            }
        }
        BigDecimal fup = unitPrice;
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO order_details (order_id, product_id, quantity, unit_price, user_id) VALUES (?, ?, ?, ?, ?)");
                    preparedStatement.setLong(1, orderId);
                    preparedStatement.setLong(2, productId);
                    preparedStatement.setInt(3, quantity);
                    preparedStatement.setBigDecimal(4, fup);
                    preparedStatement.setLong(5, user_id);
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
                loadOrderDetails();
                orderDetailsTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    private void handleRemoveProductFromTable(ActionEvent event) {
        if (orderDetailsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("ابتدا سفارشی را انتخاب کنید", "There is no selected data from tableview to delete..", Alert.AlertType.ERROR);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    OrderDetail selectedOrderDetail = orderDetailsTable.getSelectionModel().getSelectedItem();
                    if (selectedOrderDetail != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM order_details WHERE order_id = ? AND product_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedOrderDetail.getOrder().getOrderId());
                        preparedStatement.setLong(2, selectedOrderDetail.getProduct().getProductId());
                        preparedStatement.setLong(3, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        orderDetailsData.remove(selectedOrderDetail);
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
                orderDetailsTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    @FXML
    private void registerationOrder(ActionEvent event) {
        try {
            boolean userClickedSubmit = true; // user clicked on submit button
            // and all transaction will be commited.
            handleUserAction(userClickedSubmit);
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("RaziFX")
                        .text("the items submit in the order successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
            });
            previousScene(event);
        } catch (SQLException ex) {
            Logger.getLogger(OrderRegistrationController.class.getName()).log(Level.SEVERE, null, ex);
            RaziLogger.error(ex.getMessage());
        }
    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            boolean userClickedSubmit = false; // user clicked on cancel button
            // and all transaction will be rollback.
            handleUserAction(userClickedSubmit);
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("RaziFX")
                        .text("the transaction cancelled.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
            });
            previousScene(event);
        } catch (Exception e) {
            showAlert("خطا", "Failed to load main application scene.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    // previous scene means order scene
    private void previousScene(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/Ordering.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("سفارشات");
            stage.setResizable(false);
        } catch (Exception ez) {
            showAlert("خطا", "Failed to load main application scene.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + ez.getMessage());
            return;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * handle save items or not when user click on submit -> commit or cancel ->
     * rollback
     *
     * @param isSubmit boolean true if user click on submit button.
     * @throws SQLException
     */
    public void handleUserAction(boolean isSubmit) throws SQLException {
        if (isSubmit) {
            try {
                connection.commit();  // Commit the transaction (make changes permanent)
            } catch (SQLException e) {
                connection.rollback(); // Rollback if commit fails (important!)
                RaziLogger.error(e.getMessage());
                throw e;
            } finally {
                connection.setAutoCommit(true); // Re-enable auto-commit (important!)
            }

        } else { // Cancel
            connection.rollback(); // Rollback the transaction (discard changes)
            connection.setAutoCommit(true); // Re-enable auto-commit (important!)
        }
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (orderDetailsData.isEmpty()) {
            showAlert("سفارش فاقد محصول می باشد", "The order has no products.", Alert.AlertType.WARNING);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT\n"
                        + "    c.customer_id,\n"
                        + "    c.last_name,\n"
                        + "    c.phone_number,\n"
                        + "    o.order_date,\n"
                        + "    p.name,\n"
                        + "    p.price,\n"
                        + "    od.quantity,\n"
                        + "    od.unit_price,\n"
                        + "    (od.quantity * od.unit_price) AS amount_due \n"
                        + "FROM\n"
                        + "    order_details od\n"
                        + "JOIN\n"
                        + "    orders o ON od.order_id = o.order_id\n"
                        + "JOIN\n"
                        + "    customers c ON o.customer_id = c.customer_id\n"
                        + "JOIN\n"
                        + "    products p ON od.product_id = p.product_id\n"
                        + "WHERE\n"
                        + "    o.order_id ="+ order_id +" AND o.user_id="+ user_id +" AND c.user_id="+ user_id +" AND p.user_id="+ user_id +" AND od.user_id="+ user_id +";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/orders.jrxml");
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
