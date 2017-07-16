package com.github.walterfan.kanban.domain;

public class BookSearchCriteria extends BaseObject {
    private String bookSN;
    private String bookName;
    private String borrower;
    private String tags;
    // 1-in circulation 2-out circulation
    private int status = 0;
    private int days = 0;
    private int categoryID = -1;

    public String getBookSN() {
        return bookSN;
    }

    public void setBookSN(String bookSN) {
        this.bookSN = bookSN;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    /*
     * public String getReturner() { return returner; } public void
     * setReturner(String returner) { this.returner = returner; }
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int aStatus) {
        this.status = aStatus;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

}
