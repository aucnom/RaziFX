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
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import razifx.core.RaziLogger;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.Employee;
import razifx.java.model.entity.Jobs;

/**
 * SalariesController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class SalariesController implements Initializable {

    private ArrayList<String> employeeNames;
    private ArrayList<Jobs> jobList;
    private ArrayList<Employee> employeeList;

    @FXML
    private TextField searchBar;

    @FXML
    private ListView<String> listView;
    @FXML
    private Button search_btn;
    @FXML
    private Button move_main;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;

    @FXML
    void search(ActionEvent event) {
        listView.getItems().clear();
        listView.getItems().addAll(searchList(searchBar.getText(), employeeNames));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        RaziLogger.info("The salary - select employee start.");
        employeeNames = new ArrayList<>();
        jobList = new ArrayList<>();
        employeeList = new ArrayList<>();
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
        listView.getItems().addAll(employeeNames);
    }

    @FXML
    private void enter_in_leave_registeration(MouseEvent event) {
        if (listView.getSelectionModel().getSelectedItem() == null) {
            showAlert("ابتدا کارمندی را انتخاب کنید", "There is no employee to submit leave.", Alert.AlertType.INFORMATION);
            return;
        }

        Employee selectedEmployee = null;
        String selectedName = listView.getSelectionModel().getSelectedItem().toString();
        for (Employee e : employeeList) {
            if (e.getFullName().equals(selectedName)) {
                selectedEmployee = e;
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("razifx/resources/fxml/salaries_reg.fxml"));
            Parent root = (Parent) loader.load();
            SalaryRegController lc = loader.getController();
            if (selectedEmployee != null) {
                lc.setEmployee(selectedEmployee);

            }
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("ثبت حقوق");
            stage.setResizable(false);
        } catch (IOException ex) {
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
        }
    }

    private List<String> searchList(String searchWords, List<String> listOfStrings) {
        List<String> searchWordsArray = Arrays.asList(searchWords.trim().split(" "));
        return listOfStrings.stream().filter(input -> {
            return searchWordsArray.stream().allMatch(word
                    -> input.toLowerCase().contains(word.toLowerCase()));
        }).collect(Collectors.toList());
    }

    private void loadJobs() {
        try {
            jobList = new ArrayList<>();
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
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
    }

    private void loadEmployees() {
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
                employeeList.add(employee);
                employeeNames.add(employee.getFullName());
            }
        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            return;
        }
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

}
