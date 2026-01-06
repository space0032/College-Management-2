package com.college.models;

import java.time.LocalDateTime;

public class Syllabus {
    private int id;
    private int courseId;
    private String title;
    private String filePath;
    private String version;
    private String description;
    private int uploadedBy;
    private LocalDateTime uploadedAt;

    // Additional fields for display
    private String courseName;
    private String uploaderName;

    public Syllabus() {
    }

    public Syllabus(int id, int courseId, String title, String filePath, String version, String description,
            int uploadedBy, LocalDateTime uploadedAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.filePath = filePath;
        this.version = version;
        this.description = description;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
}
