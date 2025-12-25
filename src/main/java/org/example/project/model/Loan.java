package org.example.project.model;

import java.time.LocalDate;

public class Loan {
    private int id;
    private int borrowerId;
    private int bookId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    public Loan(int id, int borrowerId, int bookId, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    // Getters
    public int getId() { return id; }
    public int getBorrowerId() { return borrowerId; }
    public int getBookId() { return bookId; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}
