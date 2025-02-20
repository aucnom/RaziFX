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
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import razifx.core.RaziLogger;
import razifx.core.RegexValidator;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;

/**
 * FXML Controller class
 *
 * @author mahdihoseinzade
 */
public class ChecksReportController implements Initializable {

    @FXML
    private Button move_main;
    @FXML
    private TextField countAllR;
    @FXML
    private TextField sumAllR;
    @FXML
    private TextField avgAllR;
    @FXML
    private TextField countAllP;
    @FXML
    private TextField sumAllP;
    @FXML
    private TextField avgAllP;
    @FXML
    private PieChart checksPieChaarts;
    @FXML
    private TextField dayOfDateField1;
    @FXML
    private ComboBox<String> mounthOfDateField1;
    @FXML
    private TextField yearOfDateField1;
    @FXML
    private TextField dayOfDateField2;
    @FXML
    private ComboBox<String> mounthOfDateField2;
    @FXML
    private TextField yearOfDateField2;
    @FXML
    private Button searchR;
    @FXML
    private TextField dayOfDateField3;
    @FXML
    private ComboBox<String> mounthOfDateField3;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;
    @FXML
    private TextField yearOfDateField3;
    @FXML
    private TextField dayOfDateField4;
    @FXML
    private ComboBox<String> mounthOfDateField4;
    @FXML
    private TextField yearOfDateField4;
    @FXML
    private Button searchP;
    
    private int checkCountR;
    private int checkCountP;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The checks-received-report stage start.");
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
        mounthOfDateField3.getItems().removeAll(mounthOfDateField3.getItems());
        String[] monthOfYear = DateConvertor.getMonthOfYear();
        for (int i = 0; i < 12; i++) {
            mounthOfDateField3.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField3.getSelectionModel().select(monthOfYear[0]);

        // Initialize month combobox
        mounthOfDateField2.getItems().removeAll(mounthOfDateField2.getItems());
        for (int i = 0; i < 12; i++) {
            mounthOfDateField2.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField2.getSelectionModel().select(monthOfYear[0]);
        // Initialize month combobox
        mounthOfDateField1.getItems().removeAll(mounthOfDateField1.getItems());
        for (int i = 0; i < 12; i++) {
            mounthOfDateField1.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField1.getSelectionModel().select(monthOfYear[0]);
        // Initialize month combobox
        mounthOfDateField4.getItems().removeAll(mounthOfDateField4.getItems());
        for (int i = 0; i < 12; i++) {
            mounthOfDateField4.getItems().add(monthOfYear[i]);
        }
        mounthOfDateField4.getSelectionModel().select(monthOfYear[0]);

        String sqlPayee = "SELECT COALESCE(COUNT(*), 0) AS total_checks, COALESCE(SUM(amount), 0) AS total_amount, COALESCE(AVG(amount), 0) AS average_amount FROM checks_payee WHERE user_id= ?";
        String sqlReceived = "SELECT COALESCE(COUNT(*),0) AS total_checks, COALESCE(SUM(amount),0) AS total_amount, COALESCE(AVG(amount),0) AS average_amount FROM checks_received WHERE user_id=?";
        try {
            preparedStatement = connection.prepareStatement(sqlPayee);
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                checkCountP = resultSet.getInt("total_checks");
                countAllP.setText( checkCountP + " ");
                sumAllP.setText(String.format("%d", (long) resultSet.getBigDecimal("total_amount").doubleValue()));
                avgAllP.setText(String.format("%d", (long) resultSet.getBigDecimal("average_amount").doubleValue()));
            }
            preparedStatement = connection.prepareStatement(sqlReceived);
            preparedStatement.setLong(1, user_id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                checkCountR = resultSet.getInt("total_checks");
                countAllR.setText( checkCountR + " ");
                sumAllR.setText(String.format("%d", (long) resultSet.getBigDecimal("total_amount").doubleValue()));
                avgAllR.setText(String.format("%d", (long) resultSet.getBigDecimal("average_amount").doubleValue()));
            }
        } catch (SQLException e) {
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
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

    /**
     * search received check between 2 dates.
     * @param event 
     */
    @FXML
    private void searchingR(ActionEvent event) {
        if (checkCountR==0) {
            showAlert("هیچ چکی وجود ندارد", null, Alert.AlertType.INFORMATION);
            return;
        }
        if (dayOfDateField1.getText().isEmpty() || dayOfDateField2.getText().isEmpty() || mounthOfDateField1.getValue() == null || mounthOfDateField2.getValue() == null
                || yearOfDateField1.getText().isEmpty() || yearOfDateField2.getText().isEmpty()) {
            showAlert("تاریخ را به صورت کامل وارد کنید", "Please enter the ate with year month and day", Alert.AlertType.WARNING);
            return;
        }
        // convert date
        String day = dayOfDateField1.getText();
        String month = mounthOfDateField1.getValue().toString();
        String year = yearOfDateField1.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth = DateConvertor.getMonthOfYear(month);
        LocalDate gDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

        // convert date
        String day2 = dayOfDateField2.getText();
        String month2 = mounthOfDateField2.getValue().toString();
        String year2 = yearOfDateField2.getText();
        if (!RegexValidator.isNumericDay(day2) || !RegexValidator.isNumericYear(year2)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth2 = DateConvertor.getMonthOfYear(month2);
        LocalDate gDate2 = DateConvertor.jalaliToGregorian(Integer.parseInt(year2), numberOfMonth2, Integer.parseInt(day2));

        if (Integer.parseInt(year)>Integer.parseInt(year2)) {
            showAlert("تاریخ اشتباه", "begin date is greater than end date.", Alert.AlertType.WARNING);
            return;
        }
        if (gDate.equals(gDate2)) {
            showAlert("تاریخ اشتباه", "the same dates!", Alert.AlertType.WARNING);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT *\n"
                        + "FROM checks_received\n"
                        + "WHERE check_date BETWEEN '" + gDate +"' AND '" + gDate2 +"' AND user_id = " + user_id +";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/checkR.jrxml");
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
                RaziLogger.error("An error occured during check received reporting.");
                showAlert("خطا", "Failed to Obtain data from db.", Alert.AlertType.ERROR);
                return;
            }
        });
    }
    
    /**
     * search payee check between 2 dates.
     * @param event 
     */
    @FXML
    private void searchingP(ActionEvent event) {
        if (checkCountP==0) {
            showAlert("هیچ چکی وجود ندارد", null, Alert.AlertType.INFORMATION);
            return;
        }
        if (dayOfDateField3.getText().isEmpty() || dayOfDateField4.getText().isEmpty() || mounthOfDateField3.getValue() == null || mounthOfDateField4.getValue() == null
                || yearOfDateField3.getText().isEmpty() || yearOfDateField4.getText().isEmpty()) {
            showAlert("تاریخ را به صورت کامل وارد کنید", "Please enter the ate with year month and day", Alert.AlertType.WARNING);
            return;
        }
        // convert date
        String day = dayOfDateField3.getText();
        String month = mounthOfDateField3.getValue().toString();
        String year = yearOfDateField3.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth = DateConvertor.getMonthOfYear(month);
        LocalDate gDate = DateConvertor.jalaliToGregorian(Integer.parseInt(year), numberOfMonth, Integer.parseInt(day));

        // convert date
        String day2 = dayOfDateField4.getText();
        String month2 = mounthOfDateField4.getValue().toString();
        String year2 = yearOfDateField4.getText();
        if (!RegexValidator.isNumericDay(day) || !RegexValidator.isNumericYear(year)) {
            showAlert("تاریخ اشتباه", "Please enter the correct date day and year", Alert.AlertType.WARNING);
            return;
        }
        int numberOfMonth2 = DateConvertor.getMonthOfYear(month2);
        LocalDate gDate2 = DateConvertor.jalaliToGregorian(Integer.parseInt(year2), numberOfMonth2, Integer.parseInt(day2));
        if (Integer.parseInt(year)>Integer.parseInt(year2)) {
            showAlert("تاریخ اشتباه", "begin date is greater than end date.", Alert.AlertType.WARNING);
            return;
        }
        if (gDate.equals(gDate2)) {
            showAlert("تاریخ اشتباه", "the same dates!", Alert.AlertType.WARNING);
            return;
        }
        Platform.runLater(() -> {
            try {
                // Fetch Data from Database
                String sqlQuery = "SELECT *\n"
                        + "FROM checks_payee\n"
                        + "WHERE check_date BETWEEN '" + gDate +"' AND '" + gDate2 +"' AND user_id = " + user_id +";";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);

                // Create JasperReports Data Source
                JRResultSetDataSource dataSource = new JRResultSetDataSource(resultSet);

                // Compile Report (if needed)
                InputStream jasperStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("razifx/core/report/checkP.jrxml");
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
                RaziLogger.error("An error occured during check payee reporting.");
                showAlert("خطا", "Failed to Obtain data from db.", Alert.AlertType.ERROR);
                return;
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

}
/**
 *
 * SELECT c.first_name, c.last_name, COUNT(cr.check_id) AS number_of_checks,
 * SUM(cr.amount) AS total_amount_received FROM checks_received cr JOIN
 * customers c ON cr.customer_id = c.customer_id GROUP BY c.first_name,
 * c.last_name ORDER BY c.last_name, c.first_name; -- Optional: Order the
 * results
 */
