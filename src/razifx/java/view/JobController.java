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
import razifx.java.model.entity.Jobs;
import razifx.java.view.login.UsersController;

/**
 * JobController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class JobController implements Initializable {

    /**
     * Using for get connection to db
     */
    private DBConnector db;

    @FXML
    private TableView<Jobs> jobsTable;

    @FXML
    private TableColumn<Jobs, Long> idColumn;

    @FXML
    private TableColumn<Jobs, String> titleColumn;

    @FXML
    private TableColumn<Jobs, String> basicSalaryColumn;

    @FXML
    private TextField titleField;

    @FXML
    private TextField basicSalaryField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button move_main;

    private ObservableList<Jobs> jobsData;

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
        RaziLogger.info("The job stage start.");
        db = new DBConnector();
        connection = db.connect();
        try {
            preparedStatement = connection.prepareStatement("SELECT user_id FROM users WHERE username=? ");
            preparedStatement.setString(1, UsersController.currentUser.getUserName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                user_id = resultSet.getLong("user_id");
            }
        } catch (SQLException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("jobId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        basicSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("formattedBasicSalary"));
        // Load initial data from database
        loadJobs();
        /**
         * load data from table when user double clicked on it.
         */
        jobsTable.setRowFactory(tv -> {
            TableRow<Jobs> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Jobs data = row.getItem();
                    titleField.setText(data.getTitle());
                    basicSalaryField.setText(data.getBaseSalary().toString());
                }
            });
            return row;
        });
    }

    private void loadJobs() {
        Task<ObservableList<Jobs>> loadTask = new Task<ObservableList<Jobs>>() {
            @Override
            protected ObservableList<Jobs> call() throws Exception {
                List<Jobs> jobList = new ArrayList<>();
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM jobs WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        Jobs job = new Jobs();
                        job.setJobId(resultSet.getLong("job_id"));
                        job.setTitle(resultSet.getString("title"));
                        job.setBaseSalary(resultSet.getBigDecimal("base_salary"));
                        jobList.add(job);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 249");
                }
                return FXCollections.observableArrayList(jobList);
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
                jobsData = loadTask.getValue();
                jobsTable.setItems(jobsData);
            });
        });
        new Thread(loadTask).start();
    }

    @FXML
    void saveJob(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        String title = titleField.getText();
        if (!RegexValidator.isValidAmount(basicSalaryField.getText())) {
            showAlert("مبلغ نادرست", "please enter the correct amount", Alert.AlertType.WARNING);
            return;
        }
        BigDecimal basicSalary = new BigDecimal(basicSalaryField.getText());
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO jobs (title, base_salary, user_id) VALUES (?, ?, ?)");
                    preparedStatement.setString(1, title);
                    preparedStatement.setBigDecimal(2, basicSalary);
                    preparedStatement.setLong(3, user_id);
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
                loadJobs();
                jobsTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void deleteJob(ActionEvent event) {
        if (jobsTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک شغل را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Jobs selectedJob = jobsTable.getSelectionModel().getSelectedItem();
                    if (selectedJob != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM jobs WHERE job_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedJob.getJobId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        jobsData.remove(selectedJob);
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
                jobsTable.refresh();
            });
        });
        new Thread(deleteTask).start();
    }

    private void clearFields() {
        titleField.setText("");
        basicSalaryField.setText("");
    }

    @FXML
    private void moveToMain(ActionEvent event) {
        try {
            if (jobsData.isEmpty()) {
                showAlert("شغلی وجود ندارد", "Please Insert some job", Alert.AlertType.WARNING);
                return;
            }
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
        if (basicSalaryField.getText().isEmpty() || titleField.getText().isEmpty()) {
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
        if (jobsData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT * FROM jobs WHERE user_id = " + user_id + " ;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/jobs.jrxml");
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
