package models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "orders_selections_by_point", schema = "internova_db", catalog = "")
public class OrdersSelectionsByPointEntity {
    private long id;
    private Long orderId;
    private String title;
    private Long orderScheduleId;
    private Long orderWaypointId;
    private String type;
    private Integer quantity;

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
    @Column(name = "order_id")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
    @Column(name = "order_schedule_id")
    public Long getOrderScheduleId() {
        return orderScheduleId;
    }

    public void setOrderScheduleId(Long orderScheduleId) {
        this.orderScheduleId = orderScheduleId;
    }

    @Basic
    @Column(name = "order_waypoint_id")
    public Long getOrderWaypointId() {
        return orderWaypointId;
    }

    public void setOrderWaypointId(Long orderWaypointId) {
        this.orderWaypointId = orderWaypointId;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdersSelectionsByPointEntity that = (OrdersSelectionsByPointEntity) o;
        return id == that.id &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(orderScheduleId, that.orderScheduleId) &&
                Objects.equals(orderWaypointId, that.orderWaypointId) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, title, orderScheduleId, orderWaypointId, type);
    }

    @Basic
    @Column(name = "quantity")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
