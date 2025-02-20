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
import org.controlsfx.control.Notifications;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Employee;
import razifx.java.model.entity.Jobs;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 * EmployeesController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class EmployeesController implements Initializable {

    @FXML
    private TableView<Employee> employeesTable;

    @FXML
    private TableColumn<Employee, Long> employeeIdColumn;

    @FXML
    private TableColumn<Employee, String> jobTitleColumn;

    @FXML
    private TableColumn<Employee, String> firstNameColumn;

    @FXML
    private TableColumn<Employee, String> lastNameColumn;

    @FXML
    private TableColumn<Employee, String> nationalIdColumn;

    @FXML
    private TableColumn<Employee, String> birthDateColumn;

    @FXML
    private TableColumn<Employee, String> phoneNumberColumn;

    @FXML
    private TableColumn<Employee, String> addressColumn;

    @FXML
    private TableColumn<Employee, String> genderColumn;

    @FXML
    private TableColumn<Employee, String> baseSalaryColumn;

    @FXML
    private ComboBox<String> jobIdComboBox;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField nationalIdField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;
    /**
     * An array list of job to hold jobs table data.
     */
    private List<Jobs> jobList;

    private ObservableList<Employee> employeesData;
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
    private TextField jobIdField;
    @FXML
    private TextField dayOfDateField;
    @FXML
    private ComboBox<String> mounthOfDateField;
    @FXML
    private TextField yearOfDateField;
    @FXML
    private TextArea addressField;
    @FXML
    private Button reportButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The employee management stage start.");
        db = new DBConnector();
        connection = db.connect();
        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }
        // Load job titles into the ComboBox
        loadJobs();
        // Load initial data from database
        loadEmployees();
        // Initialize table columns
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        jobTitleColumn.setCellValueFactory(new PropertyValueFactory<>("formattedJobTitle"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        nationalIdColumn.setCellValueFactory(new PropertyValueFactory<>("nationalId"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("formattedGenderType"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("formattedBaseSalary"));
        genderComboBox.getItems().removeAll(genderComboBox.getItems());
        genderComboBox.getItems().addAll("مرد", "زن");
        genderComboBox.getSelectionModel().select("مرد");
        jobIdComboBox.getItems().removeAll(jobIdComboBox.getItems());
        jobList.stream().forEach((j) -> {
            jobIdComboBox.getItems().add(j.getTitle());
        });

        // Initialize month combobox
        mounthOfDateField.getItems().removeAll(mounthOfDateField.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField.getSelectionModel().select(monthOfYear[0]);

        jobIdComboBox.getSelectionModel().select(jobList.get(0).getTitle());
        /**
         * load data from table when user double clicked on it.
         */
        employeesTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Employee data = row.getItem();
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

    private void loadJobs() {
        try {
            jobList = new ArrayList<>();
            Jobs job;
            preparedStatement = connection.prepareStatement("SELECT * FROM jobs WHERE user_id=?");
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                job = new Jobs();
                job.setJobId(resultSet.getLong("job_id"));
                job.setTitle(resultSet.getString("title"));
                job.setBaseSalary(resultSet.getBigDecimal("base_salary"));
                jobList.add(job);
            }
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadEmployees() {
        Task<ObservableList<Employee>> loadTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() throws Exception {
                List<Employee> employees = new ArrayList<>();
                try {
                    preparedStatement = connection.prepareStatement("SELECT employee_id, job_id, first_name, last_name, national_id, birthdate, phone_number, address, gender From employees WHERE user_id=?");
                    preparedStatement.setLong(1, user_id);
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        Employee employee = new Employee();
                        employee.setEmployeeId(resultSet.getLong("employee_id"));
                        Long job_id = resultSet.getLong("job_id");
                        jobList.stream().forEachOrdered(job -> {
                            if (job.getJobId() == job_id) {
                                employee.setJob(job);
                            }
                        });
                        employee.setFirstName(resultSet.getString("first_name"));
                        employee.setLastName(resultSet.getString("last_name"));
                        employee.setNationalId(resultSet.getString("national_id"));
                        employee.setBirthdate(resultSet.getDate("birthdate"));
                        employee.setPhoneNumber(resultSet.getString("phone_number"));
                        employee.setAddress(resultSet.getString("address"));
                        employee.setGender(Employee.Gender.valueOf(resultSet.getString("gender")));
                        employees.add(employee);
                    }
                } catch (SQLException e) {
                    throw new Exception(getClass().getName() + " at line 249");
                }

                return FXCollections.observableArrayList(employees);
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
                employeesData = loadTask.getValue();
                employeesTable.setItems(employeesData);
            });
        });
        new Thread(loadTask).start();
    }

    @FXML
    void saveEmployee(ActionEvent event) {
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill the all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        final String jobTitle = jobIdComboBox.getValue();
        Long jid = null;
        for (Jobs x : jobList) {
            if (jobTitle.equals(x.getTitle())) {
                jid = x.getJobId();
            }
        }
        Long finalJobID = jid;
        final String firstName = firstNameField.getText();
        final String lastName = lastNameField.getText();
        if (!RegexValidator.isValidNationalID(nationalIdField.getText())) {
            showAlert("کدملی اشتباه", "Please enter the correct national id", Alert.AlertType.WARNING);
            return;
        }
        final String nationalId = nationalIdField.getText();
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

        final String phoneNumber = phoneNumberField.getText();
        final String address = addressField.getText();
        String gender = genderComboBox.getValue().toString();
        switch (gender) {
            case "مرد":
                gender = "MALE";
                break;
            case "زن":
                gender = "FEMALE";
                break;
        }
        String g = gender;
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO employees (job_id, first_name, last_name, national_id, birthdate, phone_number, address, gender, user_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setLong(1, finalJobID);
                    preparedStatement.setString(2, firstName);
                    preparedStatement.setString(3, lastName);
                    preparedStatement.setString(4, nationalId);
                    preparedStatement.setDate(5, java.sql.Date.valueOf(gDate));
                    preparedStatement.setString(6, phoneNumber);
                    preparedStatement.setString(7, address);
                    preparedStatement.setString(8, g);
                    preparedStatement.setLong(9, user_id);
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
                loadEmployees();
                employeesTable.refresh();
            });
        });
        new Thread(saveTask).start();
    }

    @FXML
    void updateEmployee(ActionEvent event) {
        if (employeesTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک کارمند را انتخاب کنید", "There is no selected data from tableview to update.", Alert.AlertType.WARNING);
            return;
        }
        if (!validationForm()) {
            showAlert("اطلاعات را کامل وارد کنید", "Please fill in all fields with correct values.", Alert.AlertType.WARNING);
            return;
        }
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            String jobTitle = jobIdComboBox.getValue().toString();
            Long jid = null;
            for (Jobs x : jobList) {
                if (jobTitle.equals(x.getTitle())) {
                    jid = x.getJobId();
                }
            }
            Long finalJobIDLong = jid;
            selectedEmployee.setFirstName(firstNameField.getText());
            selectedEmployee.setLastName(lastNameField.getText());
            if (!RegexValidator.isValidNationalID(nationalIdField.getText())) {
                showAlert("کدملی اشتباه", "Please enter the correct national id", Alert.AlertType.WARNING);
                return;
            }
            selectedEmployee.setNationalId(nationalIdField.getText());
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

            selectedEmployee.setBirthdate(java.sql.Date.valueOf(gDate));
            selectedEmployee.setPhoneNumber(phoneNumberField.getText());
            selectedEmployee.setAddress(addressField.getText());
            String gender = genderComboBox.getValue().toString();
            switch (gender) {
                case "مرد":
                    gender = "MALE";
                    selectedEmployee.setGender(Employee.Gender.MALE);
                    break;
                case "زن":
                    gender = "FEMALE";
                    selectedEmployee.setGender(Employee.Gender.FEMALE);
                    break;
            }
            String finalGender = gender;
            Task<Void> updateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        preparedStatement = connection.prepareStatement("UPDATE employees SET job_id = ?, first_name = ?, last_name = ?, national_id = ?, birthdate = ?, phone_number = ?, address = ?, gender = ? "
                                + "WHERE employee_id = ? AND user_id=?");
                        preparedStatement.setLong(1, finalJobIDLong);
                        preparedStatement.setString(2, selectedEmployee.getFirstName());
                        preparedStatement.setString(3, selectedEmployee.getLastName());
                        preparedStatement.setString(4, selectedEmployee.getNationalId());
                        preparedStatement.setDate(5, (Date) selectedEmployee.getBirthdate());
                        preparedStatement.setString(6, selectedEmployee.getPhoneNumber());
                        preparedStatement.setString(7, selectedEmployee.getAddress());
                        preparedStatement.setString(8, finalGender);
                        preparedStatement.setLong(9, selectedEmployee.getEmployeeId());
                        preparedStatement.setLong(10, user_id);
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
                    loadEmployees();
                    employeesTable.refresh();
                });
            });
            new Thread(updateTask).start();
        }
    }

    @FXML
    void deleteEmployee(ActionEvent event) {
        if (employeesTable.getSelectionModel().getSelectedItem() == null) {
            showAlert("یک کارمند را انتخاب کنید", "There is no selected data from tableview to delete.", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> deleteTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
                    if (selectedEmployee != null) {
                        preparedStatement = connection.prepareStatement("DELETE FROM employees WHERE employee_id = ? AND user_id=?");
                        preparedStatement.setLong(1, selectedEmployee.getEmployeeId());
                        preparedStatement.setLong(2, user_id);
                        preparedStatement.executeUpdate();
                        // Remove from table and refresh
                        employeesData.remove(selectedEmployee);
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
                employeesTable.refresh();
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
                || addressField.getText().isEmpty() || genderComboBox.getValue().isEmpty()
                || jobIdComboBox.getValue() == null || dayOfDateField.getText().isEmpty()
                || mounthOfDateField.getValue() == null || yearOfDateField.getText().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * set job id when user select the job from jobIdComboBox.
     *
     * @param event
     */
    @FXML
    private void getJobId(ActionEvent event) {
        final String jobTitle = jobIdComboBox.getValue().toString();
        for (Jobs x : jobList) {
            if (jobTitle.equals(x.getTitle())) {
                Long jid = x.getJobId();
                jobIdField.setText(jid.toString());
                break;
            }
        }
    }

    @FXML
    private void printReport(ActionEvent event) {
        if (employeesData.isEmpty()) {
            showAlert("داده ای برای نمایش وجود ندارد", "There is no data to display", Alert.AlertType.INFORMATION);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT\n"
                        + "    e.employee_id,\n"
                        + "    e.first_name,\n"
                        + "    e.last_name,\n"
                        + "    j.title AS job_title,  -- Job title from the jobs table\n"
                        + "    j.base_salary,        -- Base salary from the jobs table\n"
                        + "    e.national_id,\n"
                        + "    e.birthdate,\n"
                        + "    e.phone_number,\n"
                        + "    e.address,\n"
                        + "    e.gender\n"
                        + "FROM\n"
                        + "    employees e\n"
                        + "JOIN\n"
                        + "    jobs j ON e.job_id = j.job_id WHERE e.user_id = " + user_id + " AND j.user_id = " + user_id + " "
                        + "ORDER BY\n"
                        + "    e.employee_id;";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
//                URL url = new File("razifx/core/report/employees.jrxml").toURI().toURL();
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/employees.jrxml");
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
