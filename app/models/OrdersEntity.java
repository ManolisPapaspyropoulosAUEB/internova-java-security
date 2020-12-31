package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "orders", schema = "internova_db", catalog = "")
public class OrdersEntity {
    private long id;
    private Long offerId;
    private Long offerScheduleId;
    private Long customerId;
    private String fromAddress;
    private String fromCity;
    private String fromCountry;
    private String fromPostalCode;
    private String fromLattitude;
    private String fromLongtitude;
    private String toAddress;
    private String toCity;
    private String toCountry;
    private String toPostalCode;
    private String toLattitude;
    private String toLongtitude;
    private Date creationDate;
    private Date updateDate;
    private String type;
    private String status;
    private Long factoryId;
    private String offerScheduleToken;
    private String aa;
    private Long sellerId;
    private Long billingId;
    private String comments;
    private String truckTemprature;
    private Date arrivalFactoryDay;
    private String generalInstructions;

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
    @Column(name = "customer_id")
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
    @Column(name = "from_lattitude")
    public String getFromLattitude() {
        return fromLattitude;
    }

    public void setFromLattitude(String fromLattitude) {
        this.fromLattitude = fromLattitude;
    }

    @Basic
    @Column(name = "from_longtitude")
    public String getFromLongtitude() {
        return fromLongtitude;
    }

    public void setFromLongtitude(String fromLongtitude) {
        this.fromLongtitude = fromLongtitude;
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
    @Column(name = "to_lattitude")
    public String getToLattitude() {
        return toLattitude;
    }

    public void setToLattitude(String toLattitude) {
        this.toLattitude = toLattitude;
    }

    @Basic
    @Column(name = "to_longtitude")
    public String getToLongtitude() {
        return toLongtitude;
    }

    public void setToLongtitude(String toLongtitude) {
        this.toLongtitude = toLongtitude;
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

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdersEntity that = (OrdersEntity) o;
        return id == that.id &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(offerScheduleId, that.offerScheduleId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromPostalCode, that.fromPostalCode) &&
                Objects.equals(fromLattitude, that.fromLattitude) &&
                Objects.equals(fromLongtitude, that.fromLongtitude) &&
                Objects.equals(toAddress, that.toAddress) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toPostalCode, that.toPostalCode) &&
                Objects.equals(toLattitude, that.toLattitude) &&
                Objects.equals(toLongtitude, that.toLongtitude) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(type, that.type) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, offerId, offerScheduleId, customerId, fromAddress, fromCity, fromCountry, fromPostalCode, fromLattitude, fromLongtitude, toAddress, toCity, toCountry, toPostalCode, toLattitude, toLongtitude, creationDate, updateDate, type, status);
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
    @Column(name = "offer_schedule_token")
    public String getOfferScheduleToken() {
        return offerScheduleToken;
    }

    public void setOfferScheduleToken(String offerScheduleToken) {
        this.offerScheduleToken = offerScheduleToken;
    }

    @Basic
    @Column(name = "aa")
    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    @Basic
    @Column(name = "seller_id")
    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    @Basic
    @Column(name = "billing_id")
    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
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
    @Column(name = "truck_temprature")
    public String getTruckTemprature() {
        return truckTemprature;
    }

    public void setTruckTemprature(String truckTemprature) {
        this.truckTemprature = truckTemprature;
    }

    @Basic
    @Column(name = "arrival_factory_day")
    public Date getArrivalFactoryDay() {
        return arrivalFactoryDay;
    }

    public void setArrivalFactoryDay(Date arrivalFactoryDay) {
        this.arrivalFactoryDay = arrivalFactoryDay;
    }

    @Basic
    @Column(name = "general_instructions")
    public String getGeneralInstructions() {
        return generalInstructions;
    }

    public void setGeneralInstructions(String generalInstructions) {
        this.generalInstructions = generalInstructions;
    }
}

