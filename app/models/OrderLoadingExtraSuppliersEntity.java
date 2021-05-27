package models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "order_loading_extra_suppliers", schema = "internova_db", catalog = "")
public class OrderLoadingExtraSuppliersEntity {
    private long id;
    private Long supplierId;
    private Long orderLoadingId;
    private Double naulo;
    private Byte extraSupTimologioIndicator;

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
    @Column(name = "supplier_id")
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
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
    @Column(name = "naulo")
    public Double getNaulo() {
        return naulo;
    }

    public void setNaulo(Double naulo) {
        this.naulo = naulo;
    }

    @Basic
    @Column(name = "extra_sup_timologio_indicator")
    public Byte getExtraSupTimologioIndicator() {
        return extraSupTimologioIndicator;
    }

    public void setExtraSupTimologioIndicator(Byte extraSupTimologioIndicator) {
        this.extraSupTimologioIndicator = extraSupTimologioIndicator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLoadingExtraSuppliersEntity that = (OrderLoadingExtraSuppliersEntity) o;
        return id == that.id &&
                Objects.equals(supplierId, that.supplierId) &&
                Objects.equals(orderLoadingId, that.orderLoadingId) &&
                Objects.equals(naulo, that.naulo) &&
                Objects.equals(extraSupTimologioIndicator, that.extraSupTimologioIndicator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, supplierId, orderLoadingId, naulo, extraSupTimologioIndicator);
    }
}
