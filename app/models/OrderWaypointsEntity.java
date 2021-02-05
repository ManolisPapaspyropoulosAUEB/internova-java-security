package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_waypoints", schema = "internova_db", catalog = "")
public class OrderWaypointsEntity {
    private long id;
    private Long orderId;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String lattitude;
    private String longtitude;
    private Date updateDate;
    private Date creationDate;
    private Long warehouseId;
    private Long orderScheduleId;
    private Integer nestedScheduleIndicator;
    private Long offerScheduleBetweenWaypointId;

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
    @Column(name = "lattitude")
    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    @Basic
    @Column(name = "longtitude")
    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    @Basic
    @Column(name = "update_date")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
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
        OrderWaypointsEntity that = (OrderWaypointsEntity) o;
        return id == that.id &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(address, that.address) &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(lattitude, that.lattitude) &&
                Objects.equals(longtitude, that.longtitude) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, address, city, country, postalCode, lattitude, longtitude, updateDate, creationDate);
    }

    @Basic
    @Column(name = "warehouse_id")
    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
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
    @Column(name = "nested_schedule_indicator")
    public Integer getNestedScheduleIndicator() {
        return nestedScheduleIndicator;
    }

    public void setNestedScheduleIndicator(Integer nestedScheduleIndicator) {
        this.nestedScheduleIndicator = nestedScheduleIndicator;
    }

    @Basic
    @Column(name = "offer_schedule_between_waypoint_id")
    public Long getOfferScheduleBetweenWaypointId() {
        return offerScheduleBetweenWaypointId;
    }

    public void setOfferScheduleBetweenWaypointId(Long offerScheduleBetweenWaypointId) {
        this.offerScheduleBetweenWaypointId = offerScheduleBetweenWaypointId;
    }
}
