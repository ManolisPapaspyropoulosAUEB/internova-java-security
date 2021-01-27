package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "offer_schedule_between_waypoints", schema = "internova_db", catalog = "")
public class OfferScheduleBetweenWaypointsEntity {
    private long id;
    private Long offerId;
    private Long offerScheduleId;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private Date creationDate;
    private Date updateDate;
    private Integer nestedScheduleIndicator;

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
    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Basic
    @Column(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @Column(name = "country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Basic
    @Column(name = "postal_code")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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
        OfferScheduleBetweenWaypointsEntity that = (OfferScheduleBetweenWaypointsEntity) o;
        return id == that.id &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(offerScheduleId, that.offerScheduleId) &&
                Objects.equals(address, that.address) &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, offerId, offerScheduleId, address, city, country, postalCode, creationDate, updateDate);
    }

    @Basic
    @Column(name = "nested_schedule_indicator")
    public Integer getNestedScheduleIndicator() {
        return nestedScheduleIndicator;
    }

    public void setNestedScheduleIndicator(Integer nestedScheduleIndicator) {
        this.nestedScheduleIndicator = nestedScheduleIndicator;
    }
}
