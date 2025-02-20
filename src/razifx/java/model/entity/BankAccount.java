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
import java.util.Locale;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * BankAccount.java: Represents a bank account.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Long id;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "balance", precision = 13, scale = 0)
    private BigDecimal balance;

    @Column(name = "description")
    private String description;
    
    @Transient
    private StringProperty formattedBalance;
    
    /** The entity class should have a no-argument constructor.  */
    public BankAccount() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Transient
    public String getFormattedBalance() {
        this.formattedBalance = new SimpleStringProperty(formatCurrency(balance));
        return formattedBalance.get();
    }
    
    @Transient
    private String formatCurrency(BigDecimal value) {
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            return currencyFormatter.format(value);
    }
    
    /**
    * Generates a hash code for this BankAccount object based on its accountNumber and cardNumber.
    *
    * @return The hash code for this BankAccount object.
    */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.accountNumber);
        hash = 23 * hash + Objects.hashCode(this.cardNumber);
        return hash;
    }
    
     /**
     * Determines if this BankAccount object is equal to another object.
     * Two BankAccount objects are considered equal if they have the same accountNumber and
     * cardNumber.
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
        final BankAccount other = (BankAccount) obj;
        if (!Objects.equals(this.accountNumber, other.accountNumber)) {
            return false;
        }
        return Objects.equals(this.cardNumber, other.cardNumber);
    }

    @Override
    public String toString() {
        return "BankAccount{" + "id=" + id + ", bankName=" + bankName + ", accountNumber=" + accountNumber + ", cardNumber=" + cardNumber + ", accountHolderName=" + accountHolderName + ", balance=" + balance + ", description=" + description + '}';
    }
    
    
}
