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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import razifx.core.RaziLogger;

/**
 * FXML Controller class
 *
 * @author mahdihoseinzade
 * @since 1.0.10
 */
public class TutorialsController implements Initializable {

    @FXML
    private Pagination tutorialPaginations;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        RaziLogger.info("The tutorials window start.");
        tutorialPaginations.setPageCount(5);
        tutorialPaginations.setMaxPageIndicatorCount(3);
        tutorialPaginations.setPageFactory((pageIndex) -> {
            Parent page = createPage(pageIndex);
            return page;
        });
    }    

    private Parent createPage(Integer pageIndex) {
        String tutorialText = null;
        
        switch (pageIndex) {
            case 2:
                tutorialText = "اگر با خطا در بارگیری اطلاعات مواجه شدید:"
                        + "\nابتدا اتصال خود به اینترنت را بررسی کنید."
                        + "\nاگر مشکل از اتصال شما نبود باید به پیش نیازها دقت کنید و مراحل صفحه ۲ آموزش را"
                        + "\nمرور کنید."
                        + "\nمثال اگر شما کارمندی تعریف نکرده باشید قادر به ورود به صفحه مرخصی یا حقوق"
                        + "\nنخواهید بود یا اگر حساب بانکی تعریف نکرده باشید قادر به ثبت هزینه نیستید.";
                break;
            case 3:
                tutorialText = "تعریف حساب بانکی از اولویت های مهم است. اگر شما حسابی تعریف نکرده باشد قادر به ثبت هزینه یا موارد"
                        + "\nمرتبط به فروش نخواهید بود."
                        + "\nاولویت بعدی تعریف و ثبت محصولات است که شما برای ورود به پنجره های سفارشات"
                        + "\nیا پرداخت به آن نیاز دارید.";
                break;
            case 4:
                tutorialText = "در مورد طراحی گزارش ها قابل ذکر است که در بروزرسانی ها بعدی حتما بهبود خواهند یافت."
                        + "\nمشکل نمایش فونت های فارسی هنگام ذخیره به صورت pdf در آپدیت های بعدی حل خواهد شد."
                        + "\nهرگونه پیشلهاد یا انتقاد خود را می تولنید با بنده از طریق ایمیل به اشتراک بگذارید:"
                        + "\nآدرس ایمیل: mahdihoseinzade.jk@gmail.com"
                        + "\n با تشکر از شما";
                break;
            case 0:
                tutorialText = "مراحل استفاده از نرم افزار به شرح زیر است:"
                        + "\nبرای اجرای درست نرم افزار و نداشتن خطا ابتدا شغل ها را تعریف کرده"
                        + "\nسپس حساب های بانکی خود را ثبت کنید. در مرحله بعد به ثبت اطلاعات کارمندان"
                        + "\nخود بپردازید."
                        + "\nدر مرحله بعد اطلاعات مشتریان خود را ثبت کنید.سپس به تعریف محصولات خود بپردازید."
                        + "\nاولویت آخر نیز ثبت اطلاعات تامین کنندگان می باشد.";
                break;
            case 1:
                tutorialText = "مدیریت مشتریان --> ثبت شغل --> ثبت کارمند --> ثبت مرخصی + ثبت حقوق و دستمزد"
                        + "\nمدیریت حساب --> هزینه ها ٬ حقوق و دستمزد ٬ فاکتور خرید"
                        + "\nمدیریت مشتریان --> ثبت محصول --> ثبت مشتری --> ثبت سفارش --> ثبت پرداخت"
                        + "\n--> ثبت چک پرداختی"
                        + "\nمدیریت دارایی --> ثبت دارایی"
                        + "\nمدیریت تامین کنندگان --> ثبت تامین کننده --> ثبت فاکتور خرید + ثبت چک پرداختی"
                        + "\nگزارش هرینه و درآمد نیازمند ثبت داده های هزینه٬ حقوق کارمندان"
                        + "\nثبت خرید٬ثبت سفارش و پرداخت ها می باشد.";
                break;
        }
        
        
        Pane pane = new Pane();
       
        Label pageText = new Label(tutorialText);
        pageText.setFont(Font.font("Arial Black", 13));
        pageText.setAlignment(Pos.CENTER);
        pane.getChildren().add(pageText);
        return pane;
    }
    
}
