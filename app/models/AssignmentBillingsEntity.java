package models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "assignment_billings", schema = "internova_db", catalog = "")
public class AssignmentBillingsEntity {
    private long id;
    private Long assignmentId;
    private Long billingId;
    private Double naulo;

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
    @Column(name = "assignment_id")
    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
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
    @Column(name = "naulo")
    public Double getNaulo() {
        return naulo;
    }

    public void setNaulo(Double naulo) {
        this.naulo = naulo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentBillingsEntity that = (AssignmentBillingsEntity) o;
        return id == that.id &&
                Objects.equals(assignmentId, that.assignmentId) &&
                Objects.equals(billingId, that.billingId) &&
                Objects.equals(naulo, that.naulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assignmentId, billingId, naulo);
    }
}
