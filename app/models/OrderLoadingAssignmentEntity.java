package models;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "order_loading_assignment", schema = "internova_db", catalog = "")
public class OrderLoadingAssignmentEntity {
    private long id;
    private Long orderLoadingId;
    private String comments;
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
    @Column(name = "order_loading_id")
    public Long getOrderLoadingId() {
        return orderLoadingId;
    }

    public void setOrderLoadingId(Long orderLoadingId) {
        this.orderLoadingId = orderLoadingId;
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
        OrderLoadingAssignmentEntity that = (OrderLoadingAssignmentEntity) o;
        return id == that.id &&
                Objects.equals(orderLoadingId, that.orderLoadingId) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderLoadingId, comments, creationDate);
    }
}
