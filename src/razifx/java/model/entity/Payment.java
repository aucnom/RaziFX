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
package razifx.java.model.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * Payment.java: اطلاعات پرداخت
 * Represents a payment made for an order.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "amount", precision = 13, scale = 0)
    private BigDecimal amount;

    @Column(name = "discount", precision = 13, scale = 0)
    private BigDecimal discount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Transient
    private StringProperty formattedAmount;
    
    @Transient
    private StringProperty formattedDiscount;
   
    @Transient
    private Long formattedOrderID;    

    @Transient
    private String formattedPaymentDate;
    
    @Transient
    private String formattedPaymentMethod;

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, CASH, BANK_TRANSFER
    }
    
    /** The entity class should have a no-argument constructor.  */
    public Payment() {
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Transient
    public String getFormattedPaymentMethod() {
        // کارت اعتباری، کارت خوان، نقدی، حواله بانکی
        switch (paymentMethod) {
            case BANK_TRANSFER:
                formattedPaymentMethod = "حواله بانکی";
                break;
            case DEBIT_CARD:
                formattedPaymentMethod = "کارت خوان";
                break;
            case CASH:
                formattedPaymentMethod = "نقدی";
                break;
            case CREDIT_CARD:
                formattedPaymentMethod = "چک";
                break;
        }
        return formattedPaymentMethod;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    @Transient
    public String getFormattedPaymentDate() {
        this.formattedPaymentDate = DateConvertor.toJalali((java.sql.Date) paymentDate);
        return formattedPaymentDate;
    }
    
    @Transient
    public String getFormattedAmount() {
        this.formattedAmount = new SimpleStringProperty(formatCurrency(amount));
        return formattedAmount.get();
    }

    @Transient
    public String getFormattedDiscount() {
        this.formattedDiscount = new SimpleStringProperty(formatCurrency(discount));
        return formattedDiscount.get();
    }

    @Transient
    public Long getFormattedOrderID() {
        this.formattedOrderID = order.getOrderId();
        return formattedOrderID;
    }
    
    @Transient
    private String formatCurrency(BigDecimal value) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return currencyFormatter.format(value);
    }
    
    /**
    * Generates a hash code for this Payment object based on its order and amount.
    *
    * @return The hash code for this Payment object.
    */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.order);
        hash = 89 * hash + Objects.hashCode(this.paymentDate);
        hash = 89 * hash + Objects.hashCode(this.amount);
        hash = 89 * hash + Objects.hashCode(this.paymentMethod);
        return hash;
    }

    /**
     * Determines if this Payment object is equal to another object.
     * Two Payment objects are considered equal if they have the same order.
     *
     * @param obj The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Payment other = (Payment) obj;
        if (!Objects.equals(this.order, other.order)) {
            return false;
        }
        if (!Objects.equals(this.paymentDate, other.paymentDate)) {
            return false;
        }
        if (!Objects.equals(this.amount, other.amount)) {
            return false;
        }
        return this.paymentMethod == other.paymentMethod;
    }

    @Override
    public String toString() {
        return "Payment{" + "paymentId=" + paymentId + ", order=" + order + ", paymentDate=" + paymentDate + ", amount=" + amount + ", paymentMethod=" + paymentMethod + '}';
    }

}
