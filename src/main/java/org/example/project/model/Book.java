package org.example.project.model;

import java.math.BigDecimal;

public class Book {
    private int bookId;
    private String title, category, bookType;
    private Integer publisherId;
    private BigDecimal originalPrice;
    private boolean available;

    public Book(int bookId, String title, Integer publisherId, String category, String bookType,
                BigDecimal originalPrice, boolean available) {
        this.bookId = bookId; this.title = title; this.publisherId = publisherId;
        this.category = category; this.bookType = bookType; this.originalPrice = originalPrice; this.available = available;
    }
    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public Integer getPublisherId() { return publisherId; }
    public String getCategory() { return category; }
    public String getBookType() { return bookType; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public boolean isAvailable() { return available; }
}