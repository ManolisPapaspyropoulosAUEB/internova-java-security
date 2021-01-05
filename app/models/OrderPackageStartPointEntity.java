package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_package_start_point", schema = "internova_db", catalog = "")
public class OrderPackageStartPointEntity {
    private long id;
    private Long orderId;
    private Integer quantity;
    private Double unitPrice;
    private String title;
    private String type;
    private Long typeId;
    private Date creationDate;
    private Long orderPackageId;
    private Double finalUnitPrice;
    private Long measureUnitId;

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
    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Basic
    @Column(name = "quantity")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Basic
    @Column(name = "unit_price")
    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "type_id")
    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    @Basic
    @Column(name = "creation_date")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderPackageStartPointEntity that = (OrderPackageStartPointEntity) o;
        return id == that.id &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(title, that.title) &&
                Objects.equals(type, that.type) &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, quantity, unitPrice, title, type, typeId, creationDate);
    }

    @Basic
    @Column(name = "order_package_id")
    public Long getOrderPackageId() {
        return orderPackageId;
    }

    public void setOrderPackageId(Long orderPackageId) {
        this.orderPackageId = orderPackageId;
    }

    @Basic
    @Column(name = "final_unit_price")
    public Double getFinalUnitPrice() {
        return finalUnitPrice;
    }

    public void setFinalUnitPrice(Double finalUnitPrice) {
        this.finalUnitPrice = finalUnitPrice;
    }

    @Basic
    @Column(name = "measure_unit_id")
    public Long getMeasureUnitId() {
        return measureUnitId;
    }

    public void setMeasureUnitId(Long measureUnitId) {
        this.measureUnitId = measureUnitId;
    }
}
