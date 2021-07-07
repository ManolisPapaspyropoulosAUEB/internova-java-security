package models;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "offers", schema = "internova_db", catalog = "")
public class OffersEntity {
    private long id;
    private Long customerId;
    private Long sellerId;
    private String status;
    private Date offerDate;
    private Date creationDate;
    private Date updateDate;
    private Long billingId;
    private String fromCity;
    private String fromPostalCode;
    private String fromRegion;
    private String fromCountry;
    private Double fromLattitude;
    private Double fromLongtitude;
    private String toCity;
    private String toPostalCode;
    private String toRegion;
    private String toCountry;
    private Double toLattitude;
    private Double toLongtitude;
    private String comments;
    private String fromAddress;
    private String toAddress;
    private Long aa;
    private Long factoryId;
    private Long warehouseId;
    private String declineReasons;
    private Date acceptOfferDate;
    private Date sendOfferDate;
    private Long managerCustomerId;

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
    @Column(name = "customer_id")
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "offer_date")
    public Date getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(Date offerDate) {
        this.offerDate = offerDate;
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
    @Column(name = "billing_id")
    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
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
    @Column(name = "from_postal_code")
    public String getFromPostalCode() {
        return fromPostalCode;
    }

    public void setFromPostalCode(String fromPostalCode) {
        this.fromPostalCode = fromPostalCode;
    }

    @Basic
    @Column(name = "from_region")
    public String getFromRegion() {
        return fromRegion;
    }

    public void setFromRegion(String fromRegion) {
        this.fromRegion = fromRegion;
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
    @Column(name = "from_lattitude")
    public Double getFromLattitude() {
        return fromLattitude;
    }

    public void setFromLattitude(Double fromLattitude) {
        this.fromLattitude = fromLattitude;
    }

    @Basic
    @Column(name = "from_longtitude")
    public Double getFromLongtitude() {
        return fromLongtitude;
    }

    public void setFromLongtitude(Double fromLongtitude) {
        this.fromLongtitude = fromLongtitude;
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
    @Column(name = "to_postal_code")
    public String getToPostalCode() {
        return toPostalCode;
    }

    public void setToPostalCode(String toPostalCode) {
        this.toPostalCode = toPostalCode;
    }

    @Basic
    @Column(name = "to_region")
    public String getToRegion() {
        return toRegion;
    }

    public void setToRegion(String toRegion) {
        this.toRegion = toRegion;
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
    @Column(name = "to_lattitude")
    public Double getToLattitude() {
        return toLattitude;
    }

    public void setToLattitude(Double toLattitude) {
        this.toLattitude = toLattitude;
    }

    @Basic
    @Column(name = "to_longtitude")
    public Double getToLongtitude() {
        return toLongtitude;
    }

    public void setToLongtitude(Double toLongtitude) {
        this.toLongtitude = toLongtitude;
    }

    @Basic
    @Column(name = "comments")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffersEntity that = (OffersEntity) o;
        return id == that.id &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(sellerId, that.sellerId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(offerDate, that.offerDate) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(billingId, that.billingId) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(fromPostalCode, that.fromPostalCode) &&
                Objects.equals(fromRegion, that.fromRegion) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromLattitude, that.fromLattitude) &&
                Objects.equals(fromLongtitude, that.fromLongtitude) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(toPostalCode, that.toPostalCode) &&
                Objects.equals(toRegion, that.toRegion) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toLattitude, that.toLattitude) &&
                Objects.equals(toLongtitude, that.toLongtitude) &&
                Objects.equals(comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, sellerId, status, offerDate, creationDate, updateDate, billingId, fromCity, fromPostalCode, fromRegion, fromCountry, fromLattitude, fromLongtitude, toCity, toPostalCode, toRegion, toCountry, toLattitude, toLongtitude, comments);
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
    @Column(name = "to_address")
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Basic
    @Column(name = "aa")
    public Long getAa() {
        return aa;
    }

    public void setAa(Long aa) {
        this.aa = aa;
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
    @Column(name = "warehouse_id")
    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    @Basic
    @Column(name = "decline_reasons")
    public String getDeclineReasons() {
        return declineReasons;
    }

    public void setDeclineReasons(String declineReasons) {
        this.declineReasons = declineReasons;
    }

    @Basic
    @Column(name = "accept_offer_date")
    public Date getAcceptOfferDate() {
        return acceptOfferDate;
    }

    public void setAcceptOfferDate(Date acceptOfferDate) {
        this.acceptOfferDate = acceptOfferDate;
    }

    @Basic
    @Column(name = "send_offer_date")
    public Date getSendOfferDate() {
        return sendOfferDate;
    }

    public void setSendOfferDate(Date sendOfferDate) {
        this.sendOfferDate = sendOfferDate;
    }

    @Basic
    @Column(name = "manager_customer_id")
    public Long getManagerCustomerId() {
        return managerCustomerId;
    }

    public void setManagerCustomerId(Long managerCustomerId) {
        this.managerCustomerId = managerCustomerId;
    }
}
