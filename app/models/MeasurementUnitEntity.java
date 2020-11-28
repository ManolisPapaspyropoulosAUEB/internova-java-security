package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "measurement_unit", schema = "internova_db", catalog = "")
public class MeasurementUnitEntity {
    private long id;
    private String title;
    private String comments;
    private Double xIndex;
    private Double yIndex;
    private Double zIndex;
    private Date creationDate;
    private Date updateDate;
    private Double volume;

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
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    @Column(name = "x_index")
    public Double getxIndex() {
        return xIndex;
    }

    public void setxIndex(Double xIndex) {
        this.xIndex = xIndex;
    }

    @Basic
    @Column(name = "y_index")
    public Double getyIndex() {
        return yIndex;
    }

    public void setyIndex(Double yIndex) {
        this.yIndex = yIndex;
    }

    @Basic
    @Column(name = "z_index")
    public Double getzIndex() {
        return zIndex;
    }

    public void setzIndex(Double zIndex) {
        this.zIndex = zIndex;
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
        MeasurementUnitEntity that = (MeasurementUnitEntity) o;
        return id == that.id &&
                Objects.equals(title, that.title) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(xIndex, that.xIndex) &&
                Objects.equals(yIndex, that.yIndex) &&
                Objects.equals(zIndex, that.zIndex) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, comments, xIndex, yIndex, zIndex, creationDate, updateDate);
    }

    @Basic
    @Column(name = "volume")
    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }
}
