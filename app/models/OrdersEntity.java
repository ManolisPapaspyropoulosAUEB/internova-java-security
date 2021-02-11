package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "orders", schema = "internova_db", catalog = "")
public class OrdersEntity {
    private long id;
    private String aa;
    private Long sellerId;
    private Long factoryId;
    private Long billingId;
    private Long offerId;
    private Long customerId;
    private String type;
    private String status;
    private String comments;
    private String truckTemprature;
    private Date arrivalFactoryDay;
    private String generalInstructions;
    private Date updateDate;
    private Date creationDate;
    private String sender;

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
    @Column(name = "factory_id")
    public Long getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(Long factoryId) {
        this.factoryId = factoryId;
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
    @Column(name = "offer_id")
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
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
        OrdersEntity that = (OrdersEntity) o;
        return id == that.id &&
                Objects.equals(aa, that.aa) &&
                Objects.equals(sellerId, that.sellerId) &&
                Objects.equals(factoryId, that.factoryId) &&
                Objects.equals(billingId, that.billingId) &&
                Objects.equals(offerId, that.offerId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(status, that.status) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(truckTemprature, that.truckTemprature) &&
                Objects.equals(arrivalFactoryDay, that.arrivalFactoryDay) &&
                Objects.equals(generalInstructions, that.generalInstructions) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, aa, sellerId, factoryId, billingId, offerId, customerId, type, status, comments, truckTemprature, arrivalFactoryDay, generalInstructions, updateDate, creationDate);
    }

    @Basic
    @Column(name = "sender")
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
