package models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "order_distinct_item", schema = "internova_db", catalog = "")
public class OrderDistinctItemEntity {
    private long id;
    private String title;
    private Long orderScheduleId;
    private Long orderPackageId;
    private Integer fromUnit;
    private Integer toUnit;
    private Double unitPrice;
    private Long orderId;
    private String typePackage;

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
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "order_schedule_id")
    public Long getOrderScheduleId() {
        return orderScheduleId;
    }

    public void setOrderScheduleId(Long orderScheduleId) {
        this.orderScheduleId = orderScheduleId;
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
    @Column(name = "from_unit")
    public Integer getFromUnit() {
        return fromUnit;
    }

    public void setFromUnit(Integer fromUnit) {
        this.fromUnit = fromUnit;
    }

    @Basic
    @Column(name = "to_unit")
    public Integer getToUnit() {
        return toUnit;
    }

    public void setToUnit(Integer toUnit) {
        this.toUnit = toUnit;
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
    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDistinctItemEntity that = (OrderDistinctItemEntity) o;
        return id == that.id &&
                Objects.equals(title, that.title) &&
                Objects.equals(orderScheduleId, that.orderScheduleId) &&
                Objects.equals(orderPackageId, that.orderPackageId) &&
                Objects.equals(fromUnit, that.fromUnit) &&
                Objects.equals(toUnit, that.toUnit) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, orderScheduleId, orderPackageId, fromUnit, toUnit, unitPrice, orderId);
    }

    @Basic
    @Column(name = "type_package")
    public String getTypePackage() {
        return typePackage;
    }

    public void setTypePackage(String typePackage) {
        this.typePackage = typePackage;
    }
}
