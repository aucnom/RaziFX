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
 * Salary.java: اطلاعات حقوق دستمزد
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "salaries")
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Long salaryId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee; // The employee who received the salary.

    @Column(name = "amount", precision = 13, scale = 0)
    private BigDecimal amount; // The amount of the salary payment.

    @Temporal(TemporalType.DATE)
    @Column(name = "pay_date")
    private Date payDate; // The date the salary was paid.

    @Transient
    private StringProperty formattedAmount;

    @Transient
    private String formattedDate;

    @Transient
    private Long formattedEmployeeID;

    /**
     * The entity class should have a no-argument constructor.
     */
    public Salary() {
    }

    public Long getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Long salaryId) {
        this.salaryId = salaryId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
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
        this.formattedDate = DateConvertor.toJalali((java.sql.Date) payDate);
        return formattedDate;
    }

    @Transient
    public Long getFormattedEmployeeID() {
        formattedEmployeeID = employee.getEmployeeId();
        return formattedEmployeeID;
    }

    /**
     * Determines if this Salary object is equal to another object. Two Salary
     * objects are considered equal if they have the same employee and payment
     * date.
     *
     * @param o The object to compare with.
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
        final Salary other = (Salary) obj;
        if (!Objects.equals(this.employee, other.employee)) {
            return false;
        }
        return Objects.equals(this.payDate, other.payDate);
    }

    /**
     * Generates a hash code for this Salary object based on the employee and
     * payment date.
     *
     * @return The hash code for this Salary object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.employee);
        hash = 67 * hash + Objects.hashCode(this.payDate);
        return hash;
    }

    @Override
    public String toString() {
        return "Salary{" + "salaryId=" + salaryId + ", employee=" + employee + ", amount=" + amount + ", payDate=" + payDate + '}';
    }

    public void setEmployeeId(long aLong) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
