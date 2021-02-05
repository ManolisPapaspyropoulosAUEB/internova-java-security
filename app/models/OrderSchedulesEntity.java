package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_schedules", schema = "internova_db", catalog = "")
public class OrderSchedulesEntity {
    private long id;
    private Long factoryId;
    private Long orderId;
    private String fromAddress;
    private String fromCity;
    private String fromCountry;
    private String fromPostalCode;
    private String toAddress;
    private String toCity;
    private String toCountry;
    private String toPostalCode;
    private Integer primarySchedule;
    private Date creationDate;
    private Date updateDate;
    private String type;
    private Long offerId;
    private Long offerScheduleId;

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
    @Column(name = "factory_id")
    public Long getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(Long factoryId) {
        this.factoryId = factoryId;
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
    @Column(name = "from_address")
    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
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
    @Column(name = "to_address")
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
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
    @Column(name = "primary_schedule")
    public Integer getPrimarySchedule() {
        return primarySchedule;
    }

    public void setPrimarySchedule(Integer primarySchedule) {
        this.primarySchedule = primarySchedule;
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
        OrderSchedulesEntity that = (OrderSchedulesEntity) o;
        return id == that.id &&
                Objects.equals(factoryId, that.factoryId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromPostalCode, that.fromPostalCode) &&
                Objects.equals(toAddress, that.toAddress) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toPostalCode, that.toPostalCode) &&
                Objects.equals(primarySchedule, that.primarySchedule) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, factoryId, orderId, fromAddress, fromCity, fromCountry, fromPostalCode, toAddress, toCity, toCountry, toPostalCode, primarySchedule, creationDate, updateDate);
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
}
