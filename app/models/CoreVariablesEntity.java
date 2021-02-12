package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "core_variables", schema = "internova_db", catalog = "")
public class CoreVariablesEntity {
    private long id;
    private Double stackingFactor;
    private Date creationDate;
    private Date updateDate;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "stacking_factor")
    public Double getStackingFactor() {
        return stackingFactor;
    }

    public void setStackingFactor(Double stackingFactor) {
        this.stackingFactor = stackingFactor;
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
        CoreVariablesEntity that = (CoreVariablesEntity) o;
        return id == that.id &&
                Objects.equals(stackingFactor, that.stackingFactor) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stackingFactor, creationDate, updateDate);
    }
}
