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
 * Expense.java: اطلاعات هزینه ها
 * Represents an expense incurred by a business or individual.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expense_date")
    private Date expenseDate;

    @Column(name = "amount", precision = 13, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type")
    private ExpenseType expenseType;

    @Column(name = "description")
    private String description;
    
    @Transient
    private StringProperty formattedAmount;
    
    @Transient
    private String formattedDate;
    
    @Transient
    private String formattedType;
    
    public enum ExpenseType {
        REPAIRS, BILLS, OTHER
    }

    /** The entity class should have a no-argument constructor.  */
    public Expense() {
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Transient
    public String getFormattedDate() {
        this.formattedDate = DateConvertor.toJalali((java.sql.Date) expenseDate);
        return formattedDate;
    }

    @Transient
    public String getFormattedType() {
        if (expenseType.equals(ExpenseType.BILLS)) {
            formattedType = "پرداخت قبوض";
        }else if(expenseType.equals(ExpenseType.REPAIRS)) {
            formattedType = "تعمیرات";
        }else {
            formattedType = "دیگر";
        }
        return formattedType;
    }
    
    

    /**
    * Generates a hash code for this Expense object based on its expenseId.
    *
    * @return The hash code for this Expense object.
    */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.expenseId);
        return hash;
    }

    /**
     * Determines if this Expense object is equal to another object.
     * Two Expense objects are considered equal if they have the same date and type.
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
        final Expense other = (Expense) obj;
        return Objects.equals(this.expenseId, other.expenseId);
    }

    @Override
    public String toString() {
        return "Expense{" + "expenseId=" + expenseId + ", expenseDate=" + expenseDate + ", amount=" + amount + ", expenseType=" + expenseType + ", description=" + description + '}';
    }
    
}
