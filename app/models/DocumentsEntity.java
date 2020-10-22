package models;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "documents", schema = "internova_db", catalog = "")
public class DocumentsEntity {
    private int id;
    private String name;
    private String extension;
    private Date uploadDate;
    private String fullPath;
    private Long userId;
    private String originalFilename;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "extension")
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Basic
    @Column(name = "upload_date")
    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Basic
    @Column(name = "full_path")
    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Basic
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "original_filename")
    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentsEntity that = (DocumentsEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(extension, that.extension) &&
                Objects.equals(uploadDate, that.uploadDate) &&
                Objects.equals(fullPath, that.fullPath) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(originalFilename, that.originalFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, extension, uploadDate, fullPath, userId, originalFilename);
    }
}
