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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OrderDetail.java: Represents a single item within an order.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long orderDetailId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price", precision = 13, scale = 0)
    private BigDecimal unitPrice;
    
    @Transient
    private Long orderID;
    
    @Transient
    private String formattedProductName;
    
    @Transient
    private StringProperty formattedUnitPrice;
    
    @Transient
    private String orderDetailID;
    
    /** The entity class should have a no-argument constructor.  */
    public OrderDetail() {
    }

    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    @Transient
    public String getFormattedProductName() {
        formattedProductName = product.getName();
        return formattedProductName;
    }
    
    @Transient
    public String getFormattedUnitPrice() {
            this.formattedUnitPrice = new SimpleStringProperty(formatCurrency(unitPrice));
            return formattedUnitPrice.get();
    }
    
    @Transient
    private String formatCurrency(BigDecimal value) {
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            return currencyFormatter.format(value);
    }
    
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = this.unitPrice.multiply(new BigDecimal(quantity));
        return totalPrice;
    }
    
    @Transient
    public Long getOrderID() {
        this.orderID = order.getOrderId();
        return orderID;
    }
    
    @Override
    public String toString() {
        return "OrderDetail{" + "orderDetailId=" + orderDetailId + ", order=" + order + ", product=" + product + ",\n quantity=" + quantity + ", unitPrice=" + unitPrice + '}';
    }

}
