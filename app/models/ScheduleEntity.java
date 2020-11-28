package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "schedule", schema = "internova_db", catalog = "")
public class ScheduleEntity {
    private long id;
    private String fromAddress;
    private String fromCity;
    private String fromPostalCode;
    private String fromRegion;
    private String fromCountry;
    private Double fromLattitude;
    private Double fromLongtitude;
    private String toAddress;
    private String toCity;
    private String toPostalCode;
    private String toRegion;
    private String toCountry;
    private Double toLattitude;
    private Double toLongtitude;
    private Date creationDate;
    private Date updateDate;
    private Integer version;

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
    @Column(name = "version")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEntity that = (ScheduleEntity) o;
        return id == that.id &&
                Objects.equals(fromAddress, that.fromAddress) &&
                Objects.equals(fromCity, that.fromCity) &&
                Objects.equals(fromPostalCode, that.fromPostalCode) &&
                Objects.equals(fromRegion, that.fromRegion) &&
                Objects.equals(fromCountry, that.fromCountry) &&
                Objects.equals(fromLattitude, that.fromLattitude) &&
                Objects.equals(fromLongtitude, that.fromLongtitude) &&
                Objects.equals(toAddress, that.toAddress) &&
                Objects.equals(toCity, that.toCity) &&
                Objects.equals(toPostalCode, that.toPostalCode) &&
                Objects.equals(toRegion, that.toRegion) &&
                Objects.equals(toCountry, that.toCountry) &&
                Objects.equals(toLattitude, that.toLattitude) &&
                Objects.equals(toLongtitude, that.toLongtitude) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromAddress, fromCity, fromPostalCode, fromRegion, fromCountry, fromLattitude, fromLongtitude, toAddress, toCity, toPostalCode, toRegion, toCountry, toLattitude, toLongtitude, creationDate, updateDate, version);
    }
}
