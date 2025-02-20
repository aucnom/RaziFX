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

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import razifx.core.FirstRunCheck;
import razifx.core.RaziLogger;
import razifx.core.data.AppInfo;
import razifx.core.data.jalalidate.DateConvertor;
import razifx.java.model.dao.DBConnector;
import razifx.java.model.entity.User;
import razifx.java.view.login.UsersController;

/**
 * FXML Controller class MainController.java
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class MainController implements Initializable {

    private DBConnector db;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label date_time;
    @FXML
    private MenuItem bank_accounts;
    @FXML
    private MenuItem assets;
    @FXML
    private MenuItem expenses;
    @FXML
    private MenuItem employees;
    @FXML
    private MenuItem jobs;
    @FXML
    private MenuItem leaves;
    @FXML
    private MenuItem salaries;
    @FXML
    private MenuItem customers;
    @FXML
    private MenuItem payments;
    @FXML
    private MenuItem suppliers;
    @FXML
    private Label version_label_main;
    @FXML
    private MenuItem orders;
    @FXML
    private MenuItem products;
    @FXML
    private MenuItem purchase_payments;
    @FXML
    private MenuItem checks_report;
    @FXML
    private MenuItem about_razi;
    @FXML
    private Pane mainPameForWindow;
    @FXML
    private AnchorPane ap;
    @FXML
    private Label welcome_super_user;
    @FXML
    private Label welcome_company;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private PieChart pieChartBalanceAmountFromAll;

    static User admin;
    @FXML
    private MenuItem checks;
    @FXML
    private MenuItem checkpeyee;
    @FXML
    private MenuItem mincome_expense;
    @FXML
    private Label showTime;
    @FXML
    private Pane help_pane;
    @FXML
    private Button hideHelpButton;
    @FXML
    private MenuItem delete_account;
    @FXML
    private MenuItem depositChecks;
    @FXML
    private ImageView server_ad;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The main stage openned.");
        db = new DBConnector();
        connection = db.connect();
        if (FirstRunCheck.isCloseHelpPane()) {
            help_pane.setVisible(false);
        }
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username=? ");
            preparedStatement.setString(1, UsersController.currentUser.getUserName());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                UsersController.currentUser.setId(resultSet.getLong("user_id"));
                UsersController.currentUser.setFirstName(resultSet.getString("first_name"));
                UsersController.currentUser.setLastName(resultSet.getString("last_name"));
                UsersController.currentUser.setCompanyName(resultSet.getString("company_name"));
                welcome_super_user.setText(UsersController.currentUser.getFull_name());
                welcome_company.setText(UsersController.currentUser.getCompanyName());
            }
            admin = UsersController.currentUser;

        } catch (SQLException e) {
            RaziLogger.error(getClass().getName(), e);
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            return;
        }
        version_label_main.setText(AppInfo.getAppVersion());
        try {
            date_time.setText(getDate());
            Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e
                    -> showTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            ),
                    new KeyFrame(Duration.seconds(1))
            );
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        } catch (ParseException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            return;
        }
        if (!FirstRunCheck.isCloseTutorials()) {
            showTutorial();
        }

        // receive ad-image from server
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket("65.108.243.145", 8080) // Replace with server address and port
                ;InputStream inputStream = socket.getInputStream();DataInputStream dis = new DataInputStream(inputStream)) {
                    int imageSize = dis.readInt(); // Read the image size from the server
                    byte[] imageBytes = new byte[imageSize];
                    dis.readFully(imageBytes);
                    RaziLogger.info("The ad downloaded from server successful.");
                    // Convert byte array to Image object
                    Image image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(imageBytes)), null);
                    
                    // Set it to image-view
                    server_ad.setImage(image);
                    
                } catch (IOException e) {
                    server_ad.setVisible(false);
                    RaziLogger.error(MainController.this.getClass().getName() + " : " + e.getMessage());
                    RaziLogger.warn("An error occured during retreive ad from server.");
                }
            }
        });

    }

    private String getDate() throws ParseException {
        Date d = new Date();
        java.sql.Date sqlDate = new java.sql.Date(d.getTime());
        return DateConvertor.toJalali(sqlDate);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void mbank_accounts(ActionEvent event) {
        try {
            bank_accounts = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/bank_accounts.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("حسابهای  بانکی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void massets(ActionEvent event) {
        try {
            assets = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/asset_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("دارایی ها");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mexpenses(ActionEvent event) {
        try {
            expenses = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/expense_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("هزینه ها");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void memployees(ActionEvent event) {
        try {
            employees = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/employee_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("کارمندان");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mjobs(ActionEvent event) {
        try {
            jobs = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/job_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("شغل ها");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mleaves(ActionEvent event) {
        try {
            leaves = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/leaves.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("مرخصی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void msalaries(ActionEvent event) {
        try {
            salaries = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/salaries.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("حقوق و دسمتزد");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mcustomers(ActionEvent event) {
        try {
            customers = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/customer_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("مشتریان");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void morders(ActionEvent event) {
        try {
            orders = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/Ordering.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("سفارشات");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mproducts(ActionEvent event) {
        try {
            products = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/product_form.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("محصولات");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mpayments(ActionEvent event) {
        try {
            products = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/payment.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("پرداخت ها");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void msuppliers(ActionEvent event) {
        try {
            salaries = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/suppliers.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("تامین کنندگان");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mpurchase_payments(ActionEvent event) {
        try {
            purchase_payments = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/purchase_payment.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("خرید");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    // TODO
    @FXML
    private void mchecks_report(ActionEvent event) {
        try {
            about_razi = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/checksReport.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("گزارش گیری چک ");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mabout_razi(ActionEvent event) {
        try {
            about_razi = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/about.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("درباره نرم افزار");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    public static User getUser() throws NullPointerException {
        if (UsersController.currentUser != null) {
            return UsersController.currentUser;
        }
        return null;
    }

    @FXML
    private void mchecks(ActionEvent event) {
        boolean validCount = checkCustomer2CheckReceived();
        if (!validCount) {
            showAlert("ابتدا مشتری ای تعریف کنید", "Please initial cutomers first.");
            return;
        }
        try {
            checks = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/checks.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("چک های دریافتی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mcheckPayee(ActionEvent event) {
        try {
            checkpeyee = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/check_payee.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("چک های پرداختی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void incomeExpense(ActionEvent event) {
        try {
            mincome_expense = (MenuItem) event.getSource();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/select_fiscal_year.fxml"));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("انتخاب سال مالی");
            stage.setResizable(false);
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void handleHideHelpPane(ActionEvent event) {
        try {
            help_pane.setVisible(false);
            FirstRunCheck.setCloseHelpPane(true);
        } catch (Exception e) {
            RaziLogger.warn(e.getMessage());
        }
    }

    @FXML
    private void mdelete_account(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/delete_account_request.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("حذف حساب کاربری");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
            return;
        }
    }

    @FXML
    private void mdepositChecks(ActionEvent event) {
        boolean validCount = countChecks();
        if (!validCount) {
            showAlert("چکی برای وصول وجود ندارد.", "ُThere is no check to set deposit date..");
            return;
        }
        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/set_check_deposit.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("وصول چک");
                stage.setResizable(false);
                stage.centerOnScreen();
                FirstRunCheck.setCloseTutorials(true);
                stage.show();
            } catch (Exception e) {
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                return;
            }

        });
    }

    private void showTutorial() {
        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("razifx/resources/fxml/tutorials.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("آموزش استفاده");
                stage.setResizable(false);
                stage.centerOnScreen();
                FirstRunCheck.setCloseTutorials(true);
//                Thread.sleep(2500);
                stage.show();
            } catch (Exception e) {
                showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
                RaziLogger.warn(getClass().getName() + " : " + e.getMessage());
                return;
            }

        });
    }

    private boolean countChecks() {
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM checks_received WHERE user_id = " + admin.getId());
            if (resultSet.next()) {
                int count = resultSet.getInt(1); // Get the count from the first column
                return count > 0;
            } else {
                return false; // Should not happen, but handle it just in case
            }
        } catch (SQLException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            return false;
        }
    }

    private boolean countTransactions() {
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM transactions WHERE user_id = " + admin.getId());
            if (resultSet.next()) {
                int count = resultSet.getInt(1); // Get the count from the first column
                return count > 0;
            } else {
                return false; // Should not happen, but handle it just in case
            }
        } catch (SQLException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            return false;
        }
    }

    private boolean checkCustomer2CheckReceived() {
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM customers WHERE user_id = " + admin.getId());
            if (resultSet.next()) {
                int count = resultSet.getInt(1); // Get the count from the first column
                return count > 0;
            } else {
                return false; // Should not happen, but handle it just in case
            }
        } catch (SQLException ex) {
            showAlert("خطا در بارگیری اطلاعات", "Please check your internet connection.");
            RaziLogger.warn(getClass().getName() + " : " + ex.getMessage());
            return false;
        }
    }
}
