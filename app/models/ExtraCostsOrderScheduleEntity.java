package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "extra_costs_order_schedule", schema = "internova_db", catalog = "")
public class ExtraCostsOrderScheduleEntity {
    private long id;
    private Long orderId;
    private Long orderScheduleId;
    private Long offerScheduleId;
    private Long offerId;
    private Long extraCostId;
    private Double cost;
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
    @Column(name = "order_schedule_id")
    public Long getOrderScheduleId() {
        return orderScheduleId;
    }

    public void setOrderScheduleId(Long orderScheduleId) {
        this.orderScheduleId = orderScheduleId;
    }

    @Basic
    @Column(name = "offer_schedule_id")
    public Long getOfferScheduleId() {
        return offerScheduleId;
    }

    public void setOfferScheduleId(Long offerScheduleId) {
        this.offerScheduleId = offerScheduleId;
    }

    @Basic
    @Column(name = "offer_id")
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    @Basic
    @Column(name = "extra_cost_id")
    public Long getExtraCostId() {
        return extraCostId;
    }

    public void setExtraCostId(Long extraCostId) {
        this.extraCostId = extraCostId;
    }

    @Basic
    @Column(name = "cost")
    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
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
        ExtraCostsOrderScheduleEntity that = (ExtraCostsOrderScheduleEntity) o;
        return id == that.id &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(orderScheduleId, that.orderScheduleId) &&
                Objects.equals(offerScheduleId, that.offerScheduleId) &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(extraCostId, that.extraCostId) &&
                Objects.equals(cost, that.cost) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, orderScheduleId, offerScheduleId, offerId, extraCostId, cost, creationDate);
    }
}
