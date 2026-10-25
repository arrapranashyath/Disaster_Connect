package com.disasterconnect.entity;

import com.disasterconnect.entity.enums.MissingPersonStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "missing_persons")
public class MissingPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "missing_id")
    private Long missingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(name = "last_seen_location", nullable = false, length = 255)
    private String lastSeenLocation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "contact_info", nullable = false, length = 255)
    private String contactInfo;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissingPersonStatus status = MissingPersonStatus.MISSING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MissingPerson() {
    }

    public MissingPerson(User reportedBy, String name, Integer age, String gender,
                         String lastSeenLocation, String description, String contactInfo,
                         String photoUrl, MissingPersonStatus status) {
        this.reportedBy = reportedBy;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.lastSeenLocation = lastSeenLocation;
        this.description = description;
        this.contactInfo = contactInfo;
        this.photoUrl = photoUrl;
        this.status = status != null ? status : MissingPersonStatus.MISSING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = MissingPersonStatus.MISSING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getMissingId() {
        return missingId;
    }

    public void setMissingId(Long missingId) {
        this.missingId = missingId;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastSeenLocation() {
        return lastSeenLocation;
    }

    public void setLastSeenLocation(String lastSeenLocation) {
        this.lastSeenLocation = lastSeenLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public MissingPersonStatus getStatus() {
        return status;
    }

    public void setStatus(MissingPersonStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
