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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * Customer.java: مشخصات مشتری ها
 * Represents a customer with their personal information and account details.
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName; 

    @Column(name = "national_id", unique = true, nullable = false)
    private String nationalId; 

    @Temporal(TemporalType.DATE)
    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address; 

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;


    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
    
    @Transient
    private String formattedDate;
    
    @Transient
    private String formattedGenderType;
    
    public enum Gender {
        MALE, FEMALE
    }

    /** The entity class should have a no-argument constructor.  */
    public Customer() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    @Transient
    public String getFormattedDate() {
        this.formattedDate = DateConvertor.toJalali((java.sql.Date) birthdate);
        return formattedDate;
    }

    @Transient
    public String getFormattedGenderType() {
        if (gender.equals(Gender.MALE)) {
            formattedGenderType = "مرد";
        }else if (gender.equals(Gender.FEMALE)) {
            formattedGenderType = "زن";
        }
        return formattedGenderType;
    }    

     /** first name + last name -> full name */
    @Transient
    public String getFullName(){ return this.firstName + " " + this.lastName; }
    
    /**
    * Generates a hash code for this Customer object based on its nationalID.
    *
    * @return The hash code for this Customer object.
    */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.nationalId);
        return hash;
    }

     /**
     * Determines if this Customer object is equal to another object.
     * Two Customer objects are considered equal if they have the same nationalID.
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
        final Customer other = (Customer) obj;
        return Objects.equals(this.nationalId, other.nationalId);
    }

    @Override
    public String toString() {
        return "Customer{" + "customerId=" + customerId + ", firstName=" + firstName + ", lastName=" + lastName + ", nationalId=" + nationalId + ", birthdate=" + birthdate + ", phoneNumber=" + phoneNumber + ", address=" + address + ", gender=" + gender + ", orders=" + orders + '}';
    }

}