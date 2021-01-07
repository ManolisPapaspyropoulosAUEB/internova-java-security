package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "users_mellon_vehicles", schema = "internova_db", catalog = "")
public class UsersMellonVehiclesEntity {
    private long id;
    private String ofType;
    private String barcode;
    private Long station;
    private String status;
    private Double lat;
    private Double longt;
    private Date creationDate;

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
    @Column(name = "of_type")
    public String getOfType() {
        return ofType;
    }

    public void setOfType(String ofType) {
        this.ofType = ofType;
    }

    @Basic
    @Column(name = "barcode")
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Basic
    @Column(name = "station")
    public Long getStation() {
        return station;
    }

    public void setStation(Long station) {
        this.station = station;
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
    @Column(name = "lat")
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Basic
    @Column(name = "longt")
    public Double getLongt() {
        return longt;
    }

    public void setLongt(Double longt) {
        this.longt = longt;
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
        UsersMellonVehiclesEntity that = (UsersMellonVehiclesEntity) o;
        return id == that.id &&
                Objects.equals(ofType, that.ofType) &&
                Objects.equals(barcode, that.barcode) &&
                Objects.equals(station, that.station) &&
                Objects.equals(status, that.status) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(longt, that.longt) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ofType, barcode, station, status, lat, longt, creationDate);
    }
}
