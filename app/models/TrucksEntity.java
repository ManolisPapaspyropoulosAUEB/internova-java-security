package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "trucks", schema = "internova_db", catalog = "")
public class TrucksEntity {
    private long id;
    private String plateNumber;
    private Long typeTruckId;
    private String brandName;
    private String description;
    private Date creationDate;
    private Date udpateDate;
    private String trailerTrackor;

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
    @Column(name = "plate_number")
    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    @Basic
    @Column(name = "type_truck_id")
    public Long getTypeTruckId() {
        return typeTruckId;
    }

    public void setTypeTruckId(Long typeTruckId) {
        this.typeTruckId = typeTruckId;
    }

    @Basic
    @Column(name = "brand_name")
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    @Column(name = "udpate_date")
    public Date getUdpateDate() {
        return udpateDate;
    }

    public void setUdpateDate(Date udpateDate) {
        this.udpateDate = udpateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrucksEntity that = (TrucksEntity) o;
        return id == that.id &&
                Objects.equals(plateNumber, that.plateNumber) &&
                Objects.equals(typeTruckId, that.typeTruckId) &&
                Objects.equals(brandName, that.brandName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(udpateDate, that.udpateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, plateNumber, typeTruckId, brandName, description, creationDate, udpateDate);
    }

    @Basic
    @Column(name = "trailer_trackor")
    public String getTrailerTrackor() {
        return trailerTrackor;
    }

    public void setTrailerTrackor(String trailerTrackor) {
        this.trailerTrackor = trailerTrackor;
    }
}
