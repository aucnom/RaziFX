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
import java.time.LocalDate;
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
import razifx.java.model.entity.CheckPayee;

/**
 * FXML Controller class razifx.java.view.CheckPayeeController
 * CheckPayeeController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class CheckPayeeController implements Initializable {

    @FXML
    private TableView<CheckPayee> checksTable;

    @FXML
    private TableColumn<CheckPayee, Long> checkIdColumn;

    @FXML
    private TableColumn<CheckPayee, String> payeeNameColumn;

    @FXML
    private TableColumn<CheckPayee, String> checkNumberColumn;

    @FXML
    private TableColumn<CheckPayee, String> checkDateColumn;

    @FXML
    private TableColumn<CheckPayee, String> amountColumn;

    @FXML
    private TextField checkNumberField;

    @FXML
    private TextField amountField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private ObservableList<CheckPayee> checksData;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;

    @FXML
    private Button move_main;

    @FXML
    private TextField payeeNameField;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private Button clear_form;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The checks-payee management stage start.");
        // Initialize database connection
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        checkIdColumn.setCellValueFactory(new PropertyValueFactory<>("checkId"));
        payeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("check_payee"));
        checkNumberColumn.setCellValueFactory(new PropertyValueFactory<>("checkNumber"));
        checkDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCheckDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        // Load initial data from database
        loadChecks();
    }

    private void loadChecks() {
        Task<ObservableList<CheckPayee>> loadCheckPayeeTask = new Task<ObservableList<CheckPayee>>() {
            @Override
            protected ObservableList<CheckPayee> call() throws Exception {
                List<CheckPayee> checkPayeeList = new ArrayList<>();
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM checks_payee WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        CheckPayee check = new CheckPayee();
                        check.setCheckId(resultSet.getLong("check_id"));
                        check.setCheck_payee(resultSet.getString("check_payee"));
                        check.setCheckNumber(resultSet.getString("check_number"));
                        check.setCheckDate(resultSet.getDate("check_date"));
                        check.setAmount(resultSet.getBigDecimal("amount"));
                        checkPayeeList.add(check);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + ": at line 201");
                }
                return FXCollections.observableArrayList(checkPayeeList);
            }
        };
        loadCheckPayeeTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent arg0) {
                Throwable throwable = loadCheckPayeeTask.getException();
                RaziLogger.error(throwable.getMessage());
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
                return;
            }
        });
        loadCheckPayeeTask.setOnSucceeded(s -> {
            Platform.runLater(() -> {
                checksData = loadCheckPayeeTask.getValue();
                checksTable.setItems(checksData);
            });
        });
        new Thread(loadCheckPayeeTask).start();
    }

    @FXML
    void saveCheck(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String checkPayee = payeeNameField.getText();
        String checkNumber = checkNumberField.getText();
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
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO checks_payee (check_payee, check_number, check_date, amount, user_id) VALUES (?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, checkPayee);
                    preparedStatement.setString(2, checkNumber);
                    preparedStatement.setDate(3, Date.valueOf(gDate));
                    preparedStatement.setBigDecimal(4, amount);
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
                Notifications notifications = Notifications.create()
                        .title("RaziFX")
                        .text("The data saved to database successful.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(7))
                        .position(Pos.BOTTOM_RIGHT)
                        .darkStyle();
                notifications.show();
                loadChecks();
                checksTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deleteCheck(ActionEvent event) {
        if (checksTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک چک را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        CheckPayee selectedCheck = checksTable.getSelectionModel().getSelectedItem();
        if (selectedCheck != null) {
            Task<Void> deleteTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        preparedStatement = connection.prepareStatement("DELETE FROM checks_payee WHERE check_id = ? AND user_id = ?");
                        preparedStatement.setLong(1, selectedCheck.getCheckId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        checksData.remove(selectedCheck);
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
                    checksTable.refresh();
                });
            });
            new Thread(deleteTask).start();
        }
    }

    private void clearFields() {
        payeeNameField.setText("");
        checkNumberField.setText("");
        amountField.setText("");
        yearOfDateField.setText("1403");
        dayOfDateField.setText("29");
    }

    private boolean validationForm() {
        if (payeeNameField.getText().isEmpty() || checkNumberField.getText().isEmpty()
                || dayOfDateField.getText().isEmpty() || yearOfDateField.getText().isEmpty()
                || amountField.getText().isEmpty()) {
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
    private void clearFieledButton(ActionEvent event) {
        clearFields();
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (checksData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT\n"
                        + "    check_id,\n"
                        + "    check_payee,\n"
                        + "    check_number,\n"
                        + "    check_date,\n"
                        + "    amount\n"
                        + "FROM\n"
                        + "    checks_payee\n"
                        + "WHERE\n"
                        + "    user_id = " + user_id + "\n"
                        + "ORDER BY\n"
                        + "    check_date DESC;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/checks_payee.jrxml");
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
