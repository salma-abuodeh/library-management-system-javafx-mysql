package org.example.project.model;

import java.time.LocalDate;

public class Sale {
    private int saleId;
    private int bookId;
    private int borrowerId;
    private LocalDate saleDate;
    private double salePrice;

    public Sale(int saleId, int bookId, int borrowerId, LocalDate saleDate, double salePrice) {
        this.saleId = saleId;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.saleDate = saleDate;
        this.salePrice = salePrice;
    }

    public int getSaleId() { return saleId; }
    public int getBookId() { return bookId; }
    public int getBorrowerId() { return borrowerId; }
    public LocalDate getSaleDate() { return saleDate; }
    public double getSalePrice() { return salePrice; }

    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
}
