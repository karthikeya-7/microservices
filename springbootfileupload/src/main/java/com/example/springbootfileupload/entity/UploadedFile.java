package com.example.springbootfileupload.entity;

import javax.persistence.*;

@Entity
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String originalFileName;
    private String timestampFileName;
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors, getters, and setters

    public UploadedFile() {
        // Default constructor
    }

    public UploadedFile(String originalFileName, String timestampFileName, String status, User user) {
        this.originalFileName = originalFileName;
        this.timestampFileName = timestampFileName;
        this.status = status;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getTimestampFileName() {
        return timestampFileName;
    }

    public void setTimestampFileName(String timestampFileName) {
        this.timestampFileName = timestampFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
