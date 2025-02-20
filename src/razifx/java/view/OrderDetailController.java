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

import java.io.File;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import razifx.core.RaziLogger;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Order;
import razifx.java.model.entity.OrderDetail;
import razifx.java.model.entity.Product;

/**
 * OrderDetailController.java razifx.java.view.OrderDetailController
 *
 * @author mahdihoseinzade
 * @since 1.0
 * @deprecated use OrderRegistrationController instead of this controller
 */
@Deprecated
public class OrderDetailController implements Initializable {

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
    private TextField orderIdField;

    @FXML
    private TextField productIdField;

    @FXML
    private Spinner<Integer> quantityField;

    @FXML
    private Button saveOrderDetailButton;

    @FXML
    private Button deleteOrderDetailButton;

    private ObservableList<OrderDetail> orderDetailsData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Order order;

    @FXML
    private Button move_main;

    @FXML
    private TextField searchBar;

    @FXML
    private ListView<String> listView;

    @FXML
    private Button search_btn;

    private ArrayList<Product> products;

    private List<String> productNames;

    private Long user_id;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطای کاربر", "User Info cannot be obtained.", Alert.AlertType.ERROR);
            return;
        }
        // Initialize table columns
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("formattedProductName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedUnitPrice"));
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5000, 1);
        quantityField.setValueFactory(valueFactory);
        quantityField.getValueFactory().setValue(1);
        // Load Products
        products = new ArrayList<>();
        productNames = new LinkedList<>();
        loadProducts();
        products.stream().forEach(p -> productNames.add(p.getName()));
        // Load initial data from database
        loadOrderDetails();
    }

    void setOrderMethod(Order order) {
        if (order != null) {
            this.order = order;
            orderIdField.setText(order.getOrderId().toString());
        }
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        if (order != null) {
            try {
                orderDetailsData = FXCollections.observableArrayList();
                preparedStatement = connection.prepareStatement("SELECT * FROM order_details WHERE order_id = ? AND user_id=?");
                preparedStatement.setLong(1, order.getOrderId());
                preparedStatement.setLong(2, user_id);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    Long productID = resultSet.getLong("product_id");
                    Product selectedProduct = null;
                    for (Product p : products) {
                        if (productID == p.getProductId()) {
                            selectedProduct = p;
                        }
                    }
                    orderDetail.setProduct(selectedProduct);
                    orderDetail.setQuantity(resultSet.getInt("quantity"));
                    orderDetail.setUnitPrice(resultSet.getBigDecimal("unit_price"));
                    orderDetailsData.add(orderDetail);
                }
            } catch (SQLException e) {
                RaziLogger.error(getClass().getName(), e);
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
            orderDetailsTable.setItems(orderDetailsData);
        }
    }

    @FXML
    void saveOrderDetail(ActionEvent event) {
        try {
            Long orderId = order.getOrderId();
            Long productId = Long.parseLong(productIdField.getText());
            Integer quantity = quantityField.getValue();
            BigDecimal unitPrice = null;
            for (Product p : products) {
                if (productId == p.getProductId()) {
                    unitPrice = p.getPrice();
                }
            }
            preparedStatement = connection.prepareStatement("INSERT INTO order_details (order_id, product_id, quantity, unit_price, user_id) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, orderId);
            preparedStatement.setLong(2, productId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setBigDecimal(4, unitPrice);
            preparedStatement.setLong(5, user_id);
            preparedStatement.executeUpdate();
            // Clear fields and refresh table
            clearFields();
            loadOrderDetails();
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    @FXML
    void deleteOrderDetail(ActionEvent event) {
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
                orderDetailsTable.refresh();
            }
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void clearFields() {
        orderIdField.setText(order.getOrderId().toString());
        productIdField.setText("");
        quantityField.getValueFactory().setValue(1);
    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            if (connection != null || db != null) {
                connection.close();
                connection = null;
                db.disconnect();
                db = null;
            }
            products = null;
            productNames = null;
            URL url = new File("src/razifx/resources/fxml/order.fxml").toURI().toURL();
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("سفارشات");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا", "Failed to load Main application scene.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void search(ActionEvent event) {
        listView.getItems().clear();
        listView.getItems().addAll(searchList(searchBar.getText(), productNames));
    }

    private List<String> searchList(String searchWords, List<String> listOfStrings) {
        List<String> searchWordsArray = Arrays.asList(searchWords.trim().split(" "));
        return listOfStrings.stream().filter(input -> {
            return searchWordsArray.stream().allMatch(word
                    -> input.toLowerCase().contains(word.toLowerCase()));
        }).collect(Collectors.toList());
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
                products.add(product);
            }
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void getSelectedProductId(MouseEvent event) {
        Product selectedProduct = null;
        String selectedName = listView.getSelectionModel().getSelectedItem().toString();
        for (Product p : products) {
            if (p.getName().equals(selectedName)) {
                selectedProduct = p;
            }
        }
        productIdField.setText(selectedProduct.getProductId().toString());
    }
}
