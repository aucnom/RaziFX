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

import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import razifx.core.RaziLogger;
import razifx.java.model.dao.DBConnector;

/**
 * FXML Controller class razifx.java.view.ExpenseIncomeController
 * ExpenseIncomeController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class ExpenseIncomeController implements Initializable {

    @FXML
    private Button move_main;
    @FXML
    private TextField total_expenses;
    @FXML
    private TextField total_salaries;
    @FXML
    private TextField total_purchase;
    @FXML
    private TextField total_costs;
    @FXML
    private TextField total_income_2;
    @FXML
    private TextField customer_payments_2;
    @FXML
    private TextField net_income_3;

    private DBConnector db;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private Long user_id;

    private LocalDate start_date;
    private LocalDate end_date;
    private String sStart_date;
    private String sEnd_date;

    @FXML
    private TextField total_unpaid_2;
    @FXML
    private PieChart expensePieChartData;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The expense-income report stage start.");
        db = new DBConnector();
        connection = db.connect();

        if (MainController.admin != null) {
            user_id = MainController.admin.getId();
        } else {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.", Alert.AlertType.ERROR);
            RaziLogger.warn(getClass().getName() + " : " + "Can't retrive user id");
            return;
        }

        Platform.runLater(() -> {
            Date startDate = null;
            Date endDate = null;
            if (start_date != null && end_date != null) {
                startDate = Date.valueOf(start_date);
                endDate = Date.valueOf(end_date);
            }
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            CallableStatement statement = null;
            // Call the stored procedure
            try {
                statement = connection.prepareCall("{CALL calculate_total_expenses(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal te = resultSet.getBigDecimal("total_expenses");
                        if (te != null) {
                            total_expenses.setText(te.toString());
                        } else {
                            total_expenses.setText("فاقد اطلاعات");
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_total_salaries(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal ts = resultSet.getBigDecimal("total_salaries");
                        if (ts != null) {
                            total_salaries.setText(ts.toString());
                        } else {
                            total_salaries.setText("فاقد اطلاعات");
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_total_purchase_payments(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal tpp = resultSet.getBigDecimal("total_purchase_payments");
                        if (tpp != null) {
                            total_purchase.setText(tpp.toString());
                        } else {
                            total_purchase.setText("فاقد اطلاعات");
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_total_costs(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal ts = resultSet.getBigDecimal("total_costs");
                        if (ts != null) {
                            total_costs.setText(ts.toString());
                            pieChartData.add(new PieChart.Data("هزینه کل", ts.doubleValue()));
                        } else {
                            total_costs.setText("فاقد اطلاعات");
                            pieChartData.add(new PieChart.Data("No Data", 0.0));
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_total_income(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal ts = resultSet.getBigDecimal("total_amount");
                        if (ts != null) {
                            total_income_2.setText(ts.toString());
                            pieChartData.add(new PieChart.Data("درآمد کل", ts.doubleValue()));
                        } else {
                            total_income_2.setText("فاقد اطلاعات");
                            pieChartData.add(new PieChart.Data("No Data", 0.0));
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_total_payments(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal tp = resultSet.getBigDecimal("total_payments");
                        if (tp != null) {
                            customer_payments_2.setText(tp.toString());
                        } else {
                            customer_payments_2.setText("فاقد اطلاعات");
                        }
                    }
                }

                statement = connection.prepareCall("{CALL calculate_Unpaid(?, ?, ?)}");
                statement.setLong(1, user_id); // Set the employee ID parameter
                statement.setDate(2, startDate); // Set the start of fiscal year
                statement.setDate(3, endDate); // set the end of fiscal year

                // Execute the stored procedure and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        BigDecimal ts = resultSet.getBigDecimal("Unpaid");
                        if (ts != null) {
                            total_unpaid_2.setText(ts.toString());
                            pieChartData.add(new PieChart.Data("پرداخت نشده ها", ts.doubleValue()));
                        } else {
                            total_unpaid_2.setText("فاقد اطلاعات");
                            pieChartData.add(new PieChart.Data("No Data", 0.0));
                        }
                    }
                }
                if (total_expenses.getText().equals("فاقد اطلاعات") || total_salaries.getText().equals("فاقد اطلاعات") ||
                        total_purchase.getText().equals("فاقد اطلاعات") || total_costs.getText().equals("فاقد اطلاعات") ||
                        total_income_2.getText().equals("فاقد اطلاعات") || customer_payments_2.getText().equals("فاقد اطلاعات") ||
                        total_unpaid_2.getText().equals("فاقد اطلاعات")) {
                    net_income_3.setText("اطلاعات شما برای محاسبه درآمد خالص کافی نمی باشد");
                } else {
                    statement = connection.prepareCall("{CALL calculate_net_income(?, ?, ?)}");
                    statement.setLong(1, user_id); // Set the employee ID parameter
                    statement.setDate(2, startDate); // Set the start of fiscal year
                    statement.setDate(3, endDate); // set the end of fiscal year

                    // Execute the stored procedure and get the result set
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            BigDecimal ni = resultSet.getBigDecimal("net_income");
                            if (ni != null) {
                                net_income_3.setText(ni.toString());
                            } else {
                                net_income_3.setText("فاقد اطلاعات");
                            }
                        }
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
                RaziLogger.error(e.getMessage());
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(ExpenseIncomeController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            expensePieChartData.setData(pieChartData);
            expensePieChartData.setVisible(true);
        });

    }

    /**
     * Set fiscal year to report
     *
     * @param startDate start date of fiscal year
     * @param endDate end date of fiscal year
     * @param sStartDate start date of fiscal year as String
     * @param sEndDate end date of fiscal year as String
     */
    void setFiscalYear(LocalDate startDate, LocalDate endDate, String sStartDate, String sEndDate) {
        if (startDate != null && endDate != null
                && !sStartDate.isEmpty() && !sEndDate.isEmpty()) {
            start_date = startDate;
            end_date = endDate;
            sStart_date = sStartDate;
            sEnd_date = sEndDate;
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
            showAlert("خطا", "Failed to load Main application scene.", Alert.AlertType.ERROR);
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
