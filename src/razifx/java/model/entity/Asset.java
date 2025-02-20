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
 * Asset.java: اطلاعات دارایی ها
 * Represents an asset owned by the company.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long id;
    
    @Column(name = "asset_name", nullable = false, length = 255)
    private String name;

    @Column(name = "asset_type", length = 255)
    private String type;

    @Column(name = "purchase_date", nullable = false)
    private Date purchaseDate;

    @Column(name = "purchase_price", precision = 13, scale = 0, nullable = false)
    private BigDecimal purchasePrice;

    @Column(name = "storage_location", length = 255)
    private String storageLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('Active', 'Inactive', 'Depreciated', 'Disposed')")
    private AssetStatus status;
    
    @Transient
    private StringProperty formattedPrice;
    
    @Transient
    private String formattedDate;
    
    @Transient
    private String formattedType;

    public enum AssetStatus {
        Active, Inactive, Depreciated, Disposed
    }
    
    /** The entity class should have a no-argument constructor.  */
    public Asset() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    @Transient
    public String getFormattedPrice() {
        this.formattedPrice = new SimpleStringProperty(formatCurrency(purchasePrice));
        return formattedPrice.get();
    }
    
    @Transient
    private String formatCurrency(BigDecimal value) {
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            return currencyFormatter.format(value);
    }
    
    @Transient
    public String getFormattedDate() {
        this.formattedDate = DateConvertor.toJalali((java.sql.Date) purchaseDate);
        return formattedDate;
    }
    
    @Transient
    public String getFormattedType() {
        if (status.equals(AssetStatus.Active)) {
            formattedType = "فعال";
        }else if(status.equals(AssetStatus.Inactive)) {
            formattedType = "غیرفعال";
        }else if(status.equals(AssetStatus.Depreciated)){
            formattedType = "دارای استهلاک";
        }else {
            formattedType = "رها شده";
        }
        return formattedType;
    }
    

    /**
    * Generates a hash code for this Asset object based on its name, type and
    * purchaseDate.
    *
    * @return The hash code for this Asset object.
    */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.purchaseDate);
        return hash;
    }

     /**
     * Determines if this Asset object is equal to another object.
     * Two Asset objects are considered equal if they have the same name, type and
     * purchaseDate.
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
        final Asset other = (Asset) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return Objects.equals(this.purchaseDate, other.purchaseDate);
    }

    @Override
    public String toString() {
        return "Asset{" + "id=" + id + ", name=" + name + ", type=" + type + ", purchaseDate=" + purchaseDate + ", purchasePrice=" + purchasePrice + ", storageLocation=" + storageLocation + ", status=" + status + '}';
    }

}