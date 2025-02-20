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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import razifx.java.model.entity.Product;

/**
 * ProductController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class ProductController implements Initializable {

    /**
     * Using for get connection to db
     */
    private DBConnector db;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Long> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> priceColumn;

    @FXML
    private TableColumn<Product, String> descriptionColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button move_main;

    private ObservableList<Product> productData;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @FXML
    private Button clear_form;

    private Long user_id;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The product stage start.");
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPrice"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Load initial data from database
        loadProducts();
        /**
         * load data from table when user double clicked on it.
         */
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Product data = row.getItem();
                    idColumn.setText(data.getProductId().toString());
                    nameField.setText(data.getName());
                    priceField.setText(data.getPrice().toString());
                    descriptionField.setText(data.getDescription());
                }
            });
            return row;
        });
    }

    private void loadProducts() {
        Task<ObservableList<Product>> loadTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() throws Exception {
                List<Product> productList = new ArrayList<>();
                try {
                    productData = FXCollections.observableArrayList();
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
                    throw new Exception(getClass().getName() + " at line 249");
                }
                return FXCollections.observableArrayList(productList);
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
                productData = loadTask.getValue();
                productTable.setItems(productData);
            });
        });
        new Thread(loadTask).start();
    }

    @FXML
    void saveProduct(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String name = nameField.getText();
        if (!RegexValidator.isValidAmount(priceField.getText())) {
            showAlert("مبلغ نادرست", "Please Enter the correct Amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal price = new BigDecimal(priceField.getText());
        String description = descriptionField.getText();
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO products (name, price, description, user_id) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, name);
                    preparedStatement.setBigDecimal(2, price);
                    preparedStatement.setString(3, description);
                    preparedStatement.setLong(4, user_id);
                    preparedStatement.executeUpdate();
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
                loadProducts();
                productTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deleteProduct(ActionEvent event) {
        if (productTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک محصول را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
                    if (selectedProduct != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM products WHERE product_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedProduct.getProductId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        productData.remove(selectedProduct);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 230");
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
                productTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        nameField.setText("");
        priceField.setText("");
        descriptionField.setText("");
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

    private boolean validationForm() {
        // Validate input (e.g., check for empty fields)
        if (priceField.getText().isEmpty() || nameField.getText().isEmpty()) {
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
        if (productData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM products WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/products.jrxml");
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
