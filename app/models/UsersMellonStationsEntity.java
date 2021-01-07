package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "users_mellon_stations", schema = "internova_db", catalog = "")
public class UsersMellonStationsEntity {
    private long id;
    private String spotsAvailable;
    private String stationName;
    private String statusStation;
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
    @Column(name = "spots_available")
    public String getSpotsAvailable() {
        return spotsAvailable;
    }

    public void setSpotsAvailable(String spotsAvailable) {
        this.spotsAvailable = spotsAvailable;
    }

    @Basic
    @Column(name = "station_name")
    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    @Basic
    @Column(name = "status_station")
    public String getStatusStation() {
        return statusStation;
    }

    public void setStatusStation(String statusStation) {
        this.statusStation = statusStation;
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
        UsersMellonStationsEntity that = (UsersMellonStationsEntity) o;
        return id == that.id &&
                Objects.equals(spotsAvailable, that.spotsAvailable) &&
                Objects.equals(stationName, that.stationName) &&
                Objects.equals(statusStation, that.statusStation) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(longt, that.longt) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spotsAvailable, stationName, statusStation, lat, longt, creationDate);
    }
}
