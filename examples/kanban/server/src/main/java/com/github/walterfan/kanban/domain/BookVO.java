package com.github.walterfan.kanban.domain;

import java.sql.Timestamp;


public class BookVO extends BaseObject {
	private int bookID;
	private String bookSN;
	private String bookName;
	private String tags;
	private int categoryID;
	private Timestamp createTime;
	
	private int borrowLogID;
	private Timestamp borrowDate; 
	private int borrowerID ;
    private String borrowerName;
	private Timestamp returnDate ;
    
	public int getBookID() {
		return bookID;
	}
	public void setBookID(int bookID) {
		this.bookID = bookID;
	}
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
	public void setReturnDate(Timestamp returnDate) {
		this.returnDate = returnDate;
	}

	
	
    public String getBorrowerName() {
		return borrowerName;
	}
	public void setBorrowerName(String borrowerName) {
		this.borrowerName = borrowerName;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Book getBook() {
    	Book book = new Book();
    	book.setCategoryID(categoryID);
    	book.setId(bookID);
    	book.setTitle(bookName);
    	book.setIsbn(bookSN);
    	book.setTags(tags);
    	book.setCreateTime(createTime);
    	return book;    	
    }
    
    public void setBook(Book book) {
    	if(book==null) {
    		return;
    	}
    	this.setBookID(book.getId());
    	this.setBookName(book.getTitle());
    	this.setBookSN(book.getIsbn());
    	this.setTags(book.getTags());
    	this.setCreateTime(book.getCreateTime());
    	this.setCategoryID(book.getCategoryID());
    	this.setBorrowLogID(book.getBorrowLogID());
    }
    
    public BorrowLog getBorrowLog() {
    	BorrowLog borrowLog = new BorrowLog();
    	borrowLog.setBorrowLogID(borrowLogID);
    	borrowLog.setBookID(bookID);
    	borrowLog.setBorrowerID(borrowerID);
    	borrowLog.setBorrowDate(borrowDate);
    	borrowLog.setReturnDate(returnDate);
    	borrowLog.setCreateTime(createTime);
    	return borrowLog;    	
    }
    
	
    public void setBorrowLog(BorrowLog borrowLog) {  
    	if(borrowLog == null) {
    		return;
    	}
    	//this.setId(borrowLog.getId());
    	this.setBorrowLogID(borrowLog.getBorrowLogID());
    	this.setBorrowerID(borrowLog.getBorrowerID());
    	this.setBorrowDate(borrowLog.getBorrowDate());
    	this.setReturnDate(borrowLog.getReturnDate());
    	this.setCreateTime(borrowLog.getCreateTime());
    	
    }
}
