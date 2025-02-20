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

import java.math.BigDecimal;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import razifx.core.data.jalalidate.DateConvertor;

/**
 * Order.java: اطلاعات سفارشات
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @Transient
    private String formattedOrderDate;

    @Transient
    private String formattedStatus;

    @Transient
    private String formattedCustomerName;

    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    /**
     * The entity class should have a no-argument constructor.
     */
    public Order() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Transient
    public String getFormattedOrderDate() {
        this.formattedOrderDate = DateConvertor.toJalali((java.sql.Date) orderDate);
        return formattedOrderDate;
    }
    
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for(OrderDetail orderDetail: orderDetails) {
            totalPrice = totalPrice.add(orderDetail.getTotalPrice());
        }
        return totalPrice;
    }
    
    @Transient
    public String getFormattedStatus() {
        // "در انتظار"، "در حال پردازش"، "ارسال شده"، "تحویل شده"، "لغو"
        switch (status) {
            case PENDING:
                formattedStatus = "در انتظار";
                break;
            case PROCESSING:
                formattedStatus = "در حال پردازش";
                break;
            case SHIPPED:
                formattedStatus = "ارسال شده";
                break;
            case DELIVERED:
                formattedStatus = "تحویل شده";
                break;
            case CANCELLED:
                formattedStatus = "لغو";
                break;
        }
        return formattedStatus;
    }

    @Transient
    public String getFormattedCustomerName() {
        formattedCustomerName = customer.getFullName();
        return formattedCustomerName;
    }

    /**
     * Generates a hash code for this Order object based on its customer,
     * orderDate and orderDetails.
     *
     * @return The hash code for this Product object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.customer);
        hash = 97 * hash + Objects.hashCode(this.orderDate);
        hash = 97 * hash + Objects.hashCode(this.payment);
        return hash;
    }

    /**
     * Determines if this Order object is equal to another object. Two Order
     * objects are considered equal if they have the same customer, orderDate
     * and orderItems.
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
        final Order other = (Order) obj;
        if (!Objects.equals(this.customer, other.customer)) {
            return false;
        }
        if (!Objects.equals(this.orderDate, other.orderDate)) {
            return false;
        }
        return Objects.equals(this.payment, other.payment);
    }

    @Override
    public String toString() {
        return "Order{" + "orderId=" + orderId + ", customer=" + customer + ", orderDate=" + orderDate + ",\n status=" + status + ", orderDetails=" + orderDetails + ", payment=" + payment + '}';
    }

}
