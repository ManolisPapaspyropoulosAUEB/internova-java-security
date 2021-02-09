package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_packages", schema = "internova_db", catalog = "")
public class OrderPackagesEntity {
    private long id;
    private Long measureUnitId;
    private Long orderId;
    private Long orderWaypointId;
    private Integer fromUnit;
    private Integer toUnit;
    private Double unitPrice;
    private String comments;
    private Date creationDate;
    private Date updateDate;
    private Long orderScheduleId;
    private Long offerId;

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
    @Column(name = "measure_unit_id")
    public Long getMeasureUnitId() {
        return measureUnitId;
    }

    public void setMeasureUnitId(Long measureUnitId) {
        this.measureUnitId = measureUnitId;
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
    @Column(name = "order_waypoint_id")
    public Long getOrderWaypointId() {
        return orderWaypointId;
    }

    public void setOrderWaypointId(Long orderWaypointId) {
        this.orderWaypointId = orderWaypointId;
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
    @Column(name = "comments")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderPackagesEntity that = (OrderPackagesEntity) o;
        return id == that.id &&
                Objects.equals(measureUnitId, that.measureUnitId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(orderWaypointId, that.orderWaypointId) &&
                Objects.equals(fromUnit, that.fromUnit) &&
                Objects.equals(toUnit, that.toUnit) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, measureUnitId, orderId, orderWaypointId, fromUnit, toUnit, unitPrice, comments, creationDate, updateDate);
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
    @Column(name = "offer_id")
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }
}
