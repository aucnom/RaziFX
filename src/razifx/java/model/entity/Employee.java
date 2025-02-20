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
import razifx.core.RaziLogger;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * Employee.java: مشخصات کارکنان
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;
    
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

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Jobs job;

    @OneToMany(mappedBy = "employee")
    private List<Leave> leaves;

    @OneToMany(mappedBy = "employee")
    private List<Salary> salaries;
    
    @Transient
    private String formattedDate;
    
    @Transient
    private String formattedJobTitle;
    
    @Transient
    private String formattedBaseSalary;
    
    @Transient
    private String formattedGenderType;

    /**
     * Enum for Gender
     */
    public enum Gender {
        MALE, FEMALE;
    }

    /**
     * Enum for EmploymentStatus
     */
    public enum EmploymentStatus {
        ACTIVE, INACTIVE, TERMINATED
    }

    /** The entity class should have a no-argument constructor.  */
    public Employee() {
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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

    public Jobs getJob() {
        return job;
    }

    public void setJob(Jobs job) {
        this.job = job;
    }

    public List<Leave> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<Leave> leaves) {
        this.leaves = leaves;
    }

    public List<Salary> getSalaries() {
        return salaries;
    }

    public void setSalaries(List<Salary> salaries) {
        this.salaries = salaries;
    }
    
    @Transient
    public String getFormattedJobTitle(){
        try {
            formattedJobTitle = job.getTitle();
            return formattedJobTitle;
        } catch (NullPointerException e) {
            RaziLogger.error("NullPointerException: razifx.java.view.Employee: 200: getJobTitle");
        }
        return "نامشخص";
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
    
    @Transient
    public String getFormattedBaseSalary() {
        this.formattedBaseSalary = job.getFormattedBasicSalary();
        return formattedBaseSalary;
    }
    
    /** first name + last name -> full name */
    @Transient
    public String getFullName(){ return this.firstName + " " + this.lastName; }

    /**
    * Generates a hash code for this Employee object based on its nationalId.
    *
    * @return The hash code for this Employee object.
    */
    @Override    
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.nationalId);
        return hash;
    }
    
    /**
     * Determines if this Employee object is equal to another object.
     * Two Employee objects are considered equal if they have the same nationalId.
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
        final Employee other = (Employee) obj;
        return Objects.equals(this.nationalId, other.nationalId);
    }

    @Override
    public String toString() {
        return "Employee{" + "employeeId=" + employeeId + ", firstName=" + firstName + ", lastName=" + lastName + ", nationalId=" + nationalId + ", birthdate=" + birthdate + ", phoneNumber=" + phoneNumber + ", address=" + address + ", gender=" + gender + ", job=" + job + ", leaves=" + leaves + ", salaries=" + salaries + '}';
    }
    
}
