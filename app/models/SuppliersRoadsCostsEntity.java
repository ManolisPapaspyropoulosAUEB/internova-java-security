package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "suppliers_roads_costs", schema = "internova_db", catalog = "")
public class SuppliersRoadsCostsEntity {
    private long id;
    private Long customersSuppliersId;
    private String fromCountry;
    private String fromCity;
    private String toCountry;
    private String toCity;
    private Double cost;
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
    @Column(name = "customers_suppliers_id")
    public Long getCustomersSuppliersId() {
        return customersSuppliersId;
    }

    public void setCustomersSuppliersId(Long customersSuppliersId) {
        this.customersSuppliersId = customersSuppliersId;
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
    @Column(name = "from_city")
    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
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
    @Column(name = "to_city")
    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
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
        SuppliersRoadsCostsEntity that = (SuppliersRoadsCostsEntity) o;
        return id == that.id &&
                Objects.equals(customersSuppliersId, that.customersSuppliersId) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(cost, that.cost) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customersSuppliersId, fromCountry, fromCity, toCountry, toCity, cost, creationDate, updateDate);
    }
}
