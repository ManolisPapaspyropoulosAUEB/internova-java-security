package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "extra_costs_offers", schema = "internova_db", catalog = "")
public class ExtraCostsOffersEntity {
    private long id;
    private Long extraCostId;
    private Long offerId;
    private Long offerScheduleId;
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
    @Column(name = "extra_cost_id")
    public Long getExtraCostId() {
        return extraCostId;
    }

    public void setExtraCostId(Long extraCostId) {
        this.extraCostId = extraCostId;
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
    @Column(name = "offer_schedule_id")
    public Long getOfferScheduleId() {
        return offerScheduleId;
    }

    public void setOfferScheduleId(Long offerScheduleId) {
        this.offerScheduleId = offerScheduleId;
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
        ExtraCostsOffersEntity that = (ExtraCostsOffersEntity) o;
        return id == that.id &&
                Objects.equals(extraCostId, that.extraCostId) &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(offerScheduleId, that.offerScheduleId) &&
                Objects.equals(cost, that.cost) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, extraCostId, offerId, offerScheduleId, cost, creationDate);
    }
}
