package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "users_mellon", schema = "internova_db", catalog = "")
public class UsersMellonEntity {
    private long id;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private Integer status;
    private String imageUrl;
    private Integer socialAuth;
    private String socialPlatform;
    private String googleId;
    private String facebookId;
    private Date creationDate;
    private String address;
    private String phone;
    private String vehicleType;
    private String addressCity;
    private String postalCode;
    private Integer scooterInd;
    private Integer bicycleInd;
    private Integer electricBicycleInd;

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
    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Basic
    @Column(name = "password")
    public String getPassword() {
        if(password==null){
            return "";
        }else{
            return password;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Basic
    @Column(name = "social_auth")
    public Integer getSocialAuth() {
        return socialAuth;
    }

    public void setSocialAuth(Integer socialAuth) {
        this.socialAuth = socialAuth;
    }

    @Basic
    @Column(name = "social_platform")
    public String getSocialPlatform() {
        return socialPlatform;
    }

    public void setSocialPlatform(String socialPlatform) {
        this.socialPlatform = socialPlatform;
    }

    @Basic
    @Column(name = "google_id")
    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    @Basic
    @Column(name = "facebook_id")
    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
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
        UsersMellonEntity that = (UsersMellonEntity) o;
        return id == that.id &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(password, that.password) &&
                Objects.equals(email, that.email) &&
                Objects.equals(status, that.status) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(socialAuth, that.socialAuth) &&
                Objects.equals(socialPlatform, that.socialPlatform) &&
                Objects.equals(googleId, that.googleId) &&
                Objects.equals(facebookId, that.facebookId) &&
                Objects.equals(creationDate, that.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, password, email, status, imageUrl, socialAuth, socialPlatform, googleId, facebookId, creationDate);
    }

    @Basic
    @Column(name = "address")
    public String getAddress() {
        if(address==null){
            return "";
        }else{
            return address;
        }

    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Basic
    @Column(name = "phone")
    public String getPhone() {
        if(phone==null){
            return "";
        }else{
            return phone;
        }
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "vehicle_type")
    public String getVehicleType() {

        if(vehicleType==null){
            return "";
        }else{
            return vehicleType;
        }

    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    @Basic
    @Column(name = "address_city")
    public String getAddressCity() {
        if(addressCity==null){
            return "";
        }else{
            return addressCity;
        }
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    @Basic
    @Column(name = "postal_code")
    public String getPostalCode() {
        if(postalCode==null){
            return "";
        }else{
            return postalCode;
        }
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Basic
    @Column(name = "scooter_ind")
    public Integer getScooterInd() {
        return scooterInd;
    }

    public void setScooterInd(Integer scooterInd) {
        this.scooterInd = scooterInd;
    }

    @Basic
    @Column(name = "bicycle_ind")
    public Integer getBicycleInd() {
        return bicycleInd;
    }

    public void setBicycleInd(Integer bicycleInd) {
        this.bicycleInd = bicycleInd;
    }

    @Basic
    @Column(name = "electric_bicycle_ind")
    public Integer getElectricBicycleInd() {
        return electricBicycleInd;
    }

    public void setElectricBicycleInd(Integer electricBicycleInd) {
        this.electricBicycleInd = electricBicycleInd;
    }
}
