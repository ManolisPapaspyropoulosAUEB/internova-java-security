package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "orders_loading", schema = "internova_db", catalog = "")
public class OrdersLoadingEntity {
    private long id;
    private String status;
    private String fromCity;
    private String fromCountry;
    private String fromPostalCode;
    private String fromAddress;
    private String toCity;
    private String toCountry;
    private String toPostalCode;
    private String toAddress;
    private Long supplierId;
    private Long supplierTruckId;
    private Date creationDate;
    private Date updateDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "from_city")
    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    @Basic
    @Column(name = "from_country")
    public String getFromCountry() {
        return fromCountry;
    }

    public void setFromCountry(String fromCountry) {
        this.fromCountry = fromCountry;
    }

    @Basic
    @Column(name = "from_postal_code")
    public String getFromPostalCode() {
        return fromPostalCode;
    }

    public void setFromPostalCode(String fromPostalCode) {
        this.fromPostalCode = fromPostalCode;
    }

    @Basic
    @Column(name = "from_address")
    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Basic
    @Column(name = "to_city")
    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    @Basic
    @Column(name = "to_country")
    public String getToCountry() {
        return toCountry;
    }

    public void setToCountry(String toCountry) {
        this.toCountry = toCountry;
    }

    @Basic
    @Column(name = "to_postal_code")
    public String getToPostalCode() {
        return toPostalCode;
    }

    public void setToPostalCode(String toPostalCode) {
        this.toPostalCode = toPostalCode;
    }

    @Basic
    @Column(name = "to_address")
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Basic
    @Column(name = "creation_date")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Basic
    @Column(name = "update_date")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Basic
    @Column(name = "supplier_id")
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    @Basic
    @Column(name = "supplier_truck_id")
    public Long getSupplierTruckId() {
        return supplierTruckId;
    }

    public void setSupplierTruckId(Long supplierTruckId) {
        this.supplierTruckId = supplierTruckId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdersLoadingEntity that = (OrdersLoadingEntity) o;
        return id == that.id &&
                Objects.equals(status, that.status) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromPostalCode, that.fromPostalCode) &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toPostalCode, that.toPostalCode) &&
                Objects.equals(toAddress, that.toAddress) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(supplierId, that.supplierId) &&
                Objects.equals(supplierTruckId, that.supplierTruckId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, fromCity, fromCountry, fromPostalCode, fromAddress, toCity, toCountry, toPostalCode, toAddress, creationDate, updateDate, supplierId, supplierTruckId);
    }
}
