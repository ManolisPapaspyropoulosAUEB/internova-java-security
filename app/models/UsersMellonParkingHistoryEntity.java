package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "users_mellon_parking_history", schema = "internova_db", catalog = "")
public class UsersMellonParkingHistoryEntity {
    private long id;
    private Long userMellonId;
    private String barcode;
    private String station;
    private Date creationDate;
    private Double duration;
    private Date startTime;
    private Date endTime;

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
    @Column(name = "user_mellon_id")
    public Long getUserMellonId() {
        return userMellonId;
    }

    public void setUserMellonId(Long userMellonId) {
        this.userMellonId = userMellonId;
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
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
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
    @Column(name = "duration")
    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersMellonParkingHistoryEntity that = (UsersMellonParkingHistoryEntity) o;
        return id == that.id &&
                Objects.equals(userMellonId, that.userMellonId) &&
                Objects.equals(barcode, that.barcode) &&
                Objects.equals(station, that.station) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userMellonId, barcode, station, creationDate, duration);
    }

    @Basic
    @Column(name = "start_time")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
