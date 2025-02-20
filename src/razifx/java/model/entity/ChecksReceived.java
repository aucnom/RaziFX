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
import razifx.core.RaziLogger;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * ChecksReceived.java: مشخصات چک ها Represents a check issued by a customer.
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "checks_received")
public class ChecksReceived {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "check_number", nullable = false)
    private String checkNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "check_date")
    private Date checkDate;

    @Column(name = "amount", precision = 13, scale = 0)
    private BigDecimal amount;

    @Temporal(TemporalType.DATE)
    @Column(name = "deposit_date")
    private Date depositDate;
    
    @Transient
    private String formattedCustomerName;
    
    @Transient
    private String formattedCheckDate;
    
    @Transient
    private StringProperty formattedAmount;
    /**
     * The entity class should have a no-argument constructor.
     */
    public ChecksReceived() {
    }

    public Long getCheckId() {
        return checkId;
    }

    public void setCheckId(Long checkId) {
        this.checkId = checkId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDepositDate() {
        return depositDate;
    }

    @Transient
    public String getFormattedCustomerName(){
        try {
            formattedCustomerName = customer.getFullName();
            return formattedCustomerName;
        } catch (NullPointerException e) {
            RaziLogger.error("NullPointerException: razifx.java.view.Customer: 200: Customer getFullName");
        }
        return "نامشخص";
    }
    
    @Transient
    public String getFormattedCheckDate() {
        this.formattedCheckDate = DateConvertor.toJalali((java.sql.Date) checkDate);
        return formattedCheckDate;
    }
    

    @Transient
    public String getFormattedAmount() {
        this.formattedAmount = new SimpleStringProperty(formatCurrency(amount));
        return formattedAmount.get();
    }

    @Transient
    private String formatCurrency(BigDecimal value) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return currencyFormatter.format(value);
    }
    
    public void setDepositDate(Date depositDate) {
        this.depositDate = depositDate;
    }

    /**
     * Generates a hash code for this ChecksReceived object based on its
     * checkNumber.
     *
     * @return The hash code for this ChecksReceived object.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.checkNumber);
        return hash;
    }

    /**
     * Determines if this Checks object is equal to another object. Two Checks
     * objects are considered equal if they have the same checkNumbers.
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
        final ChecksReceived other = (ChecksReceived) obj;
        return Objects.equals(this.checkNumber, other.checkNumber);
    }

    @Override
    public String toString() {
        return "ChecksReceived{" + "checkId=" + checkId + ", customer=" + customer + ", checkNumber=" + checkNumber + ", checkDate=" + checkDate + ", amount=" + amount + ", depositDate=" + depositDate + '}';
    }

}
