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
import razifx.java.model.entity.Asset;

/**
 * AssetsController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class AssetsController implements Initializable {

    private DBConnector db;

    @FXML
    private TableView<Asset> assetsTable;

    @FXML
    private TableColumn<Asset, Long> assetIdColumn;

    @FXML
    private TableColumn<Asset, String> assetNameColumn;

    @FXML
    private TableColumn<Asset, String> assetTypeColumn;

    @FXML
    private TableColumn<Asset, String> purchaseDateColumn;

    @FXML
    private TableColumn<Asset, String> purchasePriceColumn;

    @FXML
    private TableColumn<Asset, String> storageLocationColumn;

    @FXML
    private TableColumn<Asset, String> statusColumn;

    @FXML
    private TextField assetNameField;

    @FXML
    private TextField assetTypeField;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private TextArea storageLocationField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Asset> assetsData;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @FXML
    private Button move_main;

    @FXML
    private Button clear_form;

    private Long user_id;
    @FXML
    private Button reportButton;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The asset management stage start.");
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
        assetIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        assetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        assetTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("formattedPrice"));
        storageLocationColumn.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("formattedType"));

        // Initialize expenseTypeComboBox
        statusComboBox.getItems().removeAll(statusComboBox.getItems());
        statusComboBox.getItems().addAll("فعال", "غیرفعال", "مستهلک", "رهاشده");
        statusComboBox.getSelectionModel().select("فعال");

        // Load initial data from database
        loadAssets();

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
        assetsTable.setRowFactory(tv -> {
            TableRow<Asset> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Asset data = row.getItem();
                    assetNameField.setText(data.getName());
                    assetTypeField.setText(data.getType());
                    purchasePriceField.setText(data.getPurchasePrice().toString());
                    storageLocationField.setText(data.getStorageLocation());
                    assetNameField.setText(data.getName());
                }
            });
            return row;
        });
    }

    private void loadAssets() {
        Task<ObservableList<Asset>> loadAssetTask = new Task<ObservableList<Asset>>() {
            @Override
            protected ObservableList<Asset> call() throws Exception {
                List<Asset> assetList = new ArrayList<>();
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM assets WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        Asset asset = new Asset();
                        asset.setId(resultSet.getLong("asset_id"));
                        asset.setName(resultSet.getString("asset_name"));
                        asset.setType(resultSet.getString("asset_type"));
                        asset.setPurchaseDate(resultSet.getDate("purchase_date"));
                        asset.setPurchasePrice(resultSet.getBigDecimal("purchase_price"));
                        asset.setStorageLocation(resultSet.getString("storage_location"));
                        asset.setStatus(Asset.AssetStatus.valueOf(resultSet.getString("status")));
                        assetList.add(asset);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + ": at line 201");

                }
                return FXCollections.observableArrayList(assetList);
            }
        };
        loadAssetTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadAssetTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        loadAssetTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                assetsData = loadAssetTask.getValue();
                assetsTable.setItems(assetsData);
            });
        });
        new Thread(loadAssetTask).start();
    }

    @FXML
    void saveAsset(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String assetName = assetNameField.getText();
        String assetType = assetTypeField.getText();
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

        if (!RegexValidator.isValidAmount(purchasePriceField.getText())) {
            showAlert("مبلغ اشتباه", "please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText());
        String storageLocation = storageLocationField.getText();
        String status = statusComboBox.getValue();
        switch (status) {
            case "فعال":
                status = "Active";
                break;
            case "غیرفعال":
                status = "Inactive";
                break;

            case "مستهلک":
                status = "Depreciated";
                break;
            case "رهاشده":
                status = "Disposed";
                break;
        }
        String finalStatus = status;
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO assets (asset_name, asset_type, purchase_date, purchase_price, storage_location, status, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, assetName);
                    preparedStatement.setString(2, assetType);
                    preparedStatement.setDate(3, java.sql.Date.valueOf(gDate));
                    preparedStatement.setBigDecimal(4, purchasePrice);
                    preparedStatement.setString(5, storageLocation);
                    preparedStatement.setString(6, finalStatus);
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
                        .text("The data submitted to database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                loadAssets();
                assetsTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void updateAsset(ActionEvent event) {
        if (assetsTable.getSelectionModel().getSelectedItem()==null) {
            showAlert("لطفا یک دارایی را انتخاب کنید", "There is no selected data from tableview to updates.", Alert.AlertType.WARNING);
            return;
        }
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        Asset selectedAsset = assetsTable.getSelectionModel().getSelectedItem();
        if (selectedAsset != null) {
            selectedAsset.setName(assetNameField.getText());
            selectedAsset.setType(assetTypeField.getText());
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

            selectedAsset.setPurchaseDate(java.sql.Date.valueOf(gDate));
            if (!RegexValidator.isValidAmount(purchasePriceField.getText())) {
                showAlert("مبلغ اشتباه", "please enter the correct amount", Alert.AlertType.WARNING);
                return;
            }
            selectedAsset.setPurchasePrice(new BigDecimal(purchasePriceField.getText()));
            selectedAsset.setStorageLocation(storageLocationField.getText());
            String status = statusComboBox.getValue();
            Asset.AssetStatus et = null;
            switch (status) {
                case "فعال":
                    et = Asset.AssetStatus.Active;
                    status = "Active";
                    break;
                case "غیرفعال":
                    et = Asset.AssetStatus.Inactive;
                    status = "Inactive";
                    break;

                case "مستهلک":
                    et = Asset.AssetStatus.Depreciated;
                    status = "Depreciated";
                    break;
                case "رهاشده":
                    et = Asset.AssetStatus.Disposed;
                    status = "Disposed";
                    break;
            }
            String finalStatus = status;
            selectedAsset.setStatus(et);
            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        preparedStatement = connection.prepareStatement("UPDATE assets SET asset_name = ?, asset_type = ?, purchase_date = ?, purchase_price = ?, storage_location = ?, status = ? WHERE asset_id = ? AND user_id=?");
                        preparedStatement.setString(1, selectedAsset.getName());
                        preparedStatement.setString(2, selectedAsset.getType());
                        preparedStatement.setDate(3, (Date) selectedAsset.getPurchaseDate());
                        preparedStatement.setBigDecimal(4, selectedAsset.getPurchasePrice());
                        preparedStatement.setString(5, selectedAsset.getStorageLocation());
                        preparedStatement.setString(6, finalStatus);
                        preparedStatement.setLong(7, selectedAsset.getId());
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
                    loadAssets();
                    assetsTable.refresh();
                });
            });
            new Thread(updateTask).start();
        }
    }

    @FXML
    void deleteAsset(ActionEvent event) {
        if (assetsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("لطفا یک دارایی را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Asset selectedAsset = assetsTable.getSelectionModel().getSelectedItem();
                    if (selectedAsset != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM assets WHERE asset_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedAsset.getId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        assetsData.remove(selectedAsset);
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
                assetsTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        assetNameField.setText("");
        assetTypeField.setText("");
        purchasePriceField.setText("");
        storageLocationField.setText("");
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

    private boolean validationForm() {
        // Validate input (e.g., check for empty fields)
        if (purchasePriceField.getText().isEmpty()
                || dayOfDateField.getText().isEmpty() || yearOfDateField.getText().isEmpty()
                || assetNameField.getText().isEmpty()
                || assetTypeField.getText().isEmpty() || storageLocationField.getText().isEmpty()) {
            return false;
        }
        return true;
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
        if (assetsData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM assets WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/assets.jrxml");
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
