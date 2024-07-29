package com.example.collectionbook.model;

public class ModelPdf {
    private String uid;
    private String id;
    private String title;
    private String author;
    private String description;
    private String uploadDate;
    private String categoryId;
    private String bookId;
    private String url;
    private String timestamp;
    private String coverUrl;
    private int accessCount;

    public ModelPdf() {
        // Default constructor required for calls to DataSnapshot.getValue(ModelPdf.class)
    }

    public ModelPdf(String uid, String id, String title, String author, String description, String uploadDate, String categoryId, String url, String timestamp, int accessCount,String bookId, String coverUrl) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.uploadDate = uploadDate;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.accessCount = accessCount;
        this.bookId = bookId;
        this.coverUrl = coverUrl;
    }

    // Getters and setters for all the fields
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }
}
