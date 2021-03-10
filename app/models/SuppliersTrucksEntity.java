package models;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "suppliers_trucks", schema = "internova_db", catalog = "")
public class SuppliersTrucksEntity {
    private long id;
    private Long customersSuppliersId;
    private Long truckId;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "customers_suppliers_id")
    public Long getCustomersSuppliersId() {
        return customersSuppliersId;
    }

    public void setCustomersSuppliersId(Long customersSuppliersId) {
        this.customersSuppliersId = customersSuppliersId;
    }

    @Basic
    @Column(name = "truck_id")
    public Long getTruckId() {
        return truckId;
    }

    public void setTruckId(Long truckId) {
        this.truckId = truckId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuppliersTrucksEntity that = (SuppliersTrucksEntity) o;
        return id == that.id &&
                Objects.equals(customersSuppliersId, that.customersSuppliersId) &&
                Objects.equals(truckId, that.truckId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customersSuppliersId, truckId);
    }
}
