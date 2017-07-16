package com.github.walterfan.kanban.dao;



import com.github.walterfan.kanban.domain.Book;
import com.github.walterfan.kanban.domain.BookSearchCriteria;
import com.github.walterfan.kanban.domain.BookVO;
import com.github.walterfan.kanban.domain.BorrowLog;

import java.util.List;


/**
 * @author walter
 *
 */
public interface BookDao extends ICRUD<Integer,Book> {
    public  int createBook(Book book);
    public  Book retrieveBook(int id);
    public  int updateBook(Book t);
    public int deleteBook(int id);
    
    public List<BookVO> listBook(int pageNo, int pageSize, BookSearchCriteria criteria);
    public List<BookVO> getUserBooks(int userID);
    public List<BookVO> getAllBooks();
    
    public int getBookCount(BookSearchCriteria criteria);
    public List<BookVO> getUserBorrowLogs(int userID);
    public List<BookVO> getBookBorrowLogs(int bookID);
    public int deleteBorrowLogs(int bookID);
    
    public int createBorrowLog(BorrowLog borrowLog);
    public BorrowLog retrieveBorrowLog(int id);
    public int updateBorrowLog(BorrowLog t);
    public int deleteBorrowLog(int id);

}
