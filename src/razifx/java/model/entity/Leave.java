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
import java.util.Objects;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * Leave.java: Represents a leave of absence taken by an employee.
 *
 * @author mahdihoseinzade.
 * @since 1.0
 */
@Entity
@Table(name = "leaves")
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Long leaveId;

    // The employee who took the leave.
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // The start date of the leave.
    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    // The end date of the leave.
    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    // The type of leave.
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;
    
    @Transient
    private String formattedStartDate;
    
    @Transient
    private String formattedEndDate;
    
    @Transient
    private String formattedLeaveType;
    
    @Transient
    private Long formattedEmployeeID;
    
    /**
     * Enumeration for leave types.
     */
    public enum LeaveType {
        SICK, CASUAL, OTHER
    }

    /** The entity class should have a no-argument constructor.  */
    public Leave() {
    }

    public Long getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Long leaveId) {
        this.leaveId = leaveId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    @Transient
    public String getFormattedStartDate() {
        this.formattedStartDate = DateConvertor.toJalali((java.sql.Date) startDate);
        return formattedStartDate;
    }
    
    @Transient
    public String getFormattedEndDate() {
        this.formattedEndDate = DateConvertor.toJalali((java.sql.Date) endDate);
        return formattedEndDate;
    }
    
    @Transient
    public String getFormattedLeaveType() {
        if (leaveType.equals(leaveType.SICK)) {
            formattedLeaveType = "بیمار";
        }else if (leaveType.equals(leaveType.CASUAL)) {
            formattedLeaveType = "معمولی";
        }else {
            formattedLeaveType = "دیگر";
        }
        return formattedLeaveType;
    }
    
    @Transient
    public Long getFormattedEmployeeID() {
        formattedEmployeeID = employee.getEmployeeId();
        return formattedEmployeeID;
    }
    
    /**
     * Determines if this Leave object is equal to another object.
     * Two Leave objects are considered equal if they have the same employee,
     * leaveType, startDate and endDate.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.employee);
        hash = 83 * hash + Objects.hashCode(this.startDate);
        hash = 83 * hash + Objects.hashCode(this.endDate);
        hash = 83 * hash + Objects.hashCode(this.leaveType);
        return hash;
    }

    /**
     * Generates a hash code for this Leave object based on the employee,
    leaveType, startDate and endDate.
     *
     * @return The hash code for this Leave object.
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
        final Leave other = (Leave) obj;
        if (!Objects.equals(this.employee, other.employee)) {
            return false;
        }
        if (!Objects.equals(this.startDate, other.startDate)) {
            return false;
        }
        if (!Objects.equals(this.endDate, other.endDate)) {
            return false;
        }
        return this.leaveType == other.leaveType;
    }

    @Override
    public String toString() {
        return "Leave{" + "leaveId=" + leaveId + ", employee=" + employee + ", startDate=" + startDate + ", endDate=" + endDate + ", leaveType=" + leaveType + '}';
    }

}
