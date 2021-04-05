package models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "orders_loading_orders_selections", schema = "internova_db", catalog = "")
public class OrdersLoadingOrdersSelectionsEntity {
    private long id;
    private Long orderLoadingId;
    private Long orderId;
    private String stage;
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
    @Column(name = "order_loading_id")
    public Long getOrderLoadingId() {
        return orderLoadingId;
    }

    public void setOrderLoadingId(Long orderLoadingId) {
        this.orderLoadingId = orderLoadingId;
    }

    @Basic
    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Basic
    @Column(name = "stage")
    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
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
        OrdersLoadingOrdersSelectionsEntity that = (OrdersLoadingOrdersSelectionsEntity) o;
        return id == that.id &&
                Objects.equals(orderLoadingId, that.orderLoadingId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(stage, that.stage) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderLoadingId, orderId, stage, creationDate, updateDate);
    }
}
