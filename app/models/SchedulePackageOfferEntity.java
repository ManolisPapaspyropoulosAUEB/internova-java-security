package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "schedule_package_offer", schema = "internova_db", catalog = "")
public class SchedulePackageOfferEntity {
    private long id;
    private Long measureUnitId;
    private Long offerId;
    private Integer fromUnit;
    private Integer toUnit;
    private Double unitPrice;
    private String comments;
    private Date creationDate;
    private Date updateDate;
    private Long offerScheduleId;
    private String typePackageMeasure;

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
    @Column(name = "offer_id")
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
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
        SchedulePackageOfferEntity that = (SchedulePackageOfferEntity) o;
        return id == that.id &&
                Objects.equals(measureUnitId, that.measureUnitId) &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(fromUnit, that.fromUnit) &&
                Objects.equals(toUnit, that.toUnit) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, measureUnitId, offerId, fromUnit, toUnit, unitPrice, comments, creationDate, updateDate);
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
    @Column(name = "type_package_measure")
    public String getTypePackageMeasure() {
        return typePackageMeasure;
    }

    public void setTypePackageMeasure(String typePackageMeasure) {
        this.typePackageMeasure = typePackageMeasure;
    }
}
