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
 * CheckPayee.java
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
//public class CheckPayee {
@Entity
@Table(name = "checks_payee")
public class CheckPayee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "check_payee")
    private String check_payee;

    @Column(name = "check_number", nullable = false)
    private String checkNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "check_date")
    private Date checkDate;

    @Column(name = "amount", precision = 13, scale = 0)
    private BigDecimal amount;
    
    @Transient
    private String formattedCheckDate;
    
    @Transient
    private StringProperty formattedAmount;
    /**
     * The entity class should have a no-argument constructor.
     */
    public CheckPayee() {
    }

    public Long getCheckId() {
        return checkId;
    }

    public void setCheckId(Long checkId) {
        this.checkId = checkId;
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

    public String getCheck_payee() {
        return check_payee;
    }

    public void setCheck_payee(String check_payee) {
        this.check_payee = check_payee;
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
        final CheckPayee other = (CheckPayee) obj;
        return Objects.equals(this.checkNumber, other.checkNumber);
    }

    @Override
    public String toString() {
        return "CheckPayee{" + "checkId=" + checkId + ", check_payee=" + check_payee + ", checkNumber=" + checkNumber + ", checkDate=" + checkDate + ", amount=" + amount + ", formattedCheckDate=" + formattedCheckDate + ", formattedAmount=" + formattedAmount + '}';
    }


}
