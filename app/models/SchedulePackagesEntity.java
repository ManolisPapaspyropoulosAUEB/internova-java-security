package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "schedule_packages", schema = "internova_db", catalog = "")
public class SchedulePackagesEntity {
    private long id;
    private Long scheduleId;
    private Long measurementUnitId;
    private Integer fromUnit;
    private Integer toUnit;
    private Double unitPrice;
    private Double summary;
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
    @Column(name = "schedule_id")
    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    @Basic
    @Column(name = "measurement_unit_id")
    public Long getMeasurementUnitId() {
        return measurementUnitId;
    }

    public void setMeasurementUnitId(Long measurementUnitId) {
        this.measurementUnitId = measurementUnitId;
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
    @Column(name = "summary")
    public Double getSummary() {
        return summary;
    }

    public void setSummary(Double summary) {
        this.summary = summary;
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
        SchedulePackagesEntity that = (SchedulePackagesEntity) o;
        return id == that.id &&
                Objects.equals(scheduleId, that.scheduleId) &&
                Objects.equals(measurementUnitId, that.measurementUnitId) &&
                Objects.equals(fromUnit, that.fromUnit) &&
                Objects.equals(toUnit, that.toUnit) &&
                Objects.equals(unitPrice, that.unitPrice) &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scheduleId, measurementUnitId, fromUnit, toUnit, unitPrice, summary, creationDate, updateDate);
    }
}
