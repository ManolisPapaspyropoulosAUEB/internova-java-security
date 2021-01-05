package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_waypoints_packages", schema = "internova_db", catalog = "")
public class OrderWaypointsPackagesEntity {
    private long id;
    private Long orderId;
    private Long measureUnitId;
    private Long orderWaypointId;
    private Integer quantity;
    private Double unitPrice;
    private Double finalUnitPrice;
    private String title;
    private Long typeId;
    private Date creationDate;

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
    @Column(name = "measure_unit_id")
    public Long getMeasureUnitId() {
        return measureUnitId;
    }

    public void setMeasureUnitId(Long measureUnitId) {
        this.measureUnitId = measureUnitId;
    }

    @Basic
    @Column(name = "order_waypoint_id")
    public Long getOrderWaypointId() {
        return orderWaypointId;
    }

    public void setOrderWaypointId(Long orderWaypointId) {
        this.orderWaypointId = orderWaypointId;
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
    @Column(name = "final_unit_price")
    public Double getFinalUnitPrice() {
        return finalUnitPrice;
    }

    public void setFinalUnitPrice(Double finalUnitPrice) {
        this.finalUnitPrice = finalUnitPrice;
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
        OrderWaypointsPackagesEntity that = (OrderWaypointsPackagesEntity) o;
        return id == that.id &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(measureUnitId, that.measureUnitId) &&
                Objects.equals(orderWaypointId, that.orderWaypointId) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(finalUnitPrice, that.finalUnitPrice) &&
                Objects.equals(title, that.title) &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, measureUnitId, orderWaypointId, quantity, unitPrice, finalUnitPrice, title, typeId, creationDate);
    }
}
