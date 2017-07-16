package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;

/**
 * @author walter
 * 
 */
public class BorrowLog extends BaseObject {
    private int borrowLogID;
    private int bookID;
    private Timestamp borrowDate;
    private int borrowerID;
    private Timestamp returnDate;
    private Timestamp createTime;

    public int getBorrowLogID() {
        return borrowLogID;
    }

    public void setBorrowLogID(int borrowLogID) {
        this.borrowLogID = borrowLogID;
    }

    public Timestamp getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Timestamp borrowDate) {
        this.borrowDate = borrowDate;
    }

    public int getBorrowerID() {
        return borrowerID;
    }

    public void setBorrowerID(int borrowerID) {
        this.borrowerID = borrowerID;
    }

    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returndate) {
        this.returnDate = returndate;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

}
