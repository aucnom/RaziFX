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
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Customer;

/**
 * CustomerController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class CustomerController implements Initializable {

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, Long> customerIdColumn;

    @FXML
    private TableColumn<Customer, String> firstNameColumn;

    @FXML
    private TableColumn<Customer, String> lastNameColumn;

    @FXML
    private TableColumn<Customer, String> nationalIdColumn;

    @FXML
    private TableColumn<Customer, String> birthDateColumn;

    @FXML
    private TableColumn<Customer, String> phoneNumberColumn;

    @FXML
    private TableColumn<Customer, String> addressColumn;

    @FXML
    private TableColumn<Customer, String> genderColumn;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField nationalIdField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextArea addressField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Customer> customersData;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The customer management stage start.");
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطای کاربر", "User Info cannot be obtained.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
        // Load initial data from database
        loadCustomers();
        // Initialize table columns
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        nationalIdColumn.setCellValueFactory(new PropertyValueFactory<>("nationalId"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("formattedGenderType"));
        genderComboBox.getItems().removeAll(genderComboBox.getItems());
        genderComboBox.getItems().addAll("مرد", "زن");
        genderComboBox.getSelectionModel().select("مرد");

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
        customersTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Customer data = row.getItem();
                    firstNameField.setText(data.getFirstName());
                    lastNameField.setText(data.getLastName());
                    nationalIdField.setText(data.getNationalId());
                    phoneNumberField.setText(data.getPhoneNumber());
                    addressField.setText(data.getAddress());
                }
            });
            return row;
        });
    }

    private void loadCustomers() {
        Task<ObservableList<Customer>> loadCustomerTask = new Task<ObservableList<Customer>>() {
            @Override
            protected ObservableList<Customer> call() throws Exception {
                List<Customer> customerList = new ArrayList<>();
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
                    throw new Exception(getClass().getName() + ": at line 201");

                }
                return FXCollections.observableArrayList(customerList);
            }
        };
        loadCustomerTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadCustomerTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            }
        });
        loadCustomerTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                customersData = loadCustomerTask.getValue();
                customersTable.setItems(customersData);
            });
        });
        new Thread(loadCustomerTask).start();
    }

    @FXML
    void saveCustomer(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String nationalId = nationalIdField.getText();
        if (!RegexValidator.isValidNationalID(nationalId)) {
            showAlert("کدملی اشتباه", "Please enter the correct national id", Alert.AlertType.WARNING);
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

        String phoneNumber = phoneNumberField.getText();
        String address = addressField.getText();
        String gender = genderComboBox.getValue().toString();
        switch (gender) {
            case "مرد":
                gender = "MALE";
                break;
            case "زن":
                gender = "FEMALE";
                break;
        }
        String finalGender = gender;
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO customers (first_name, last_name, national_id, birthdate, phone_number, address, gender, user_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, firstName);
                    preparedStatement.setString(2, lastName);
                    preparedStatement.setString(3, nationalId);
                    preparedStatement.setDate(4, java.sql.Date.valueOf(gDate));
                    preparedStatement.setString(5, phoneNumber);
                    preparedStatement.setString(6, address);
                    preparedStatement.setString(7, finalGender);
                    preparedStatement.setLong(8, user_id);
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
                loadCustomers();
                customersTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void updateCustomer(ActionEvent event) {
        if (customersTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک مشتری را انتخاب کنید", "There is no selected data from tableview to update.", Alert.AlertType.WARNING);
            return;
        }
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            selectedCustomer.setFirstName(firstNameField.getText());
            selectedCustomer.setLastName(lastNameField.getText());
            if (!RegexValidator.isValidNationalID(nationalIdField.getText())) {
                showAlert("کدملی اشتباه", "please enter the correct national id", Alert.AlertType.WARNING);
                return;
            }
            selectedCustomer.setNationalId(nationalIdField.getText());

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

            selectedCustomer.setBirthdate(java.sql.Date.valueOf(gDate));
            selectedCustomer.setPhoneNumber(phoneNumberField.getText());
            selectedCustomer.setAddress(addressField.getText());
            String gender = genderComboBox.getValue().toString();
            switch (gender) {
                case "مرد":
                    selectedCustomer.setGender(Customer.Gender.MALE);
                    gender = "MALE";
                    break;
                case "زن":
                    selectedCustomer.setGender(Customer.Gender.FEMALE);
                    gender = "FEMALE";
                    break;
            }
            String finalGender = gender;
            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        preparedStatement = connection.prepareStatement("UPDATE customers SET first_name = ?, last_name = ?, national_id = ?, birthdate = ?, phone_number = ?, address = ?, gender = ? "
                                + "WHERE customer_id = ? AND user_id=?");
                        preparedStatement.setString(1, selectedCustomer.getFirstName());
                        preparedStatement.setString(2, selectedCustomer.getLastName());
                        preparedStatement.setString(3, selectedCustomer.getNationalId());
                        preparedStatement.setDate(4, (Date) selectedCustomer.getBirthdate());
                        preparedStatement.setString(5, selectedCustomer.getPhoneNumber());
                        preparedStatement.setString(6, selectedCustomer.getAddress());
                        preparedStatement.setString(7, finalGender);
                        preparedStatement.setLong(8, selectedCustomer.getCustomerId());
                        preparedStatement.setLong(9, user_id);
                        preparedStatement.executeUpdate();
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
                    loadCustomers();
                    customersTable.refresh();
                });
            });
            new Thread(updateTask).start();
        }
    }

    @FXML
    void deleteCustomer(ActionEvent event) {
        if (customersTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک مشتری را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
                    if (selectedCustomer != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM customers WHERE customer_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedCustomer.getCustomerId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        customersData.remove(selectedCustomer);
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

                customersTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        nationalIdField.setText("");
        phoneNumberField.setText("");
        addressField.setText("");
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

    @FXML
    private void clearFieledButton(ActionEvent event) {
        clearFields();
    }

    private boolean validationForm() {
        // Validate input (e.g., check for empty fields)
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()
                || nationalIdField.getText().isEmpty() || phoneNumberField.getText().isEmpty()
                || addressField.getText().isEmpty() || dayOfDateField.getText().isEmpty()
                || yearOfDateField.getText().isEmpty() || genderComboBox.getValue() == null) {
            return false;
        }
        return true;
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (customersData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM customers WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/customers.jrxml");
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
