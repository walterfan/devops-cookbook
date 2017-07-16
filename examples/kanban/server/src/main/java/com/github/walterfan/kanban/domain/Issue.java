package com.github.walterfan.kanban.domain;


public class Issue extends BaseObject {
    private int issueID;
    private String problem;
    private String cause;
    private String solution;
    
    private String comments;
    private int categoryID;
    private int userID;
    
    /**
     * @return the issueID
     */
    public int getIssueID() {
        return issueID;
    }
    
    /**
     * @param issueID the issueID to set
     */
    public void setIssueID(int issueID) {
        this.issueID = issueID;
    }
    
    /**
     * @return the problem
     */
    public String getProblem() {
        return problem;
    }
    
    /**
     * @param problem the problem to set
     */
    public void setProblem(String problem) {
        this.problem = problem;
    }
    
    /**
     * @return the cause
     */
    public String getCause() {
        return cause;
    }
    
    /**
     * @param cause the cause to set
     */
    public void setCause(String cause) {
        this.cause = cause;
    }
    
    /**
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }
    
    /**
     * @param solution the solution to set
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }
    
    /**
     * @return the comment
     */
    public String getComments() {
        return comments;
    }
    
    /**
     * @param comment the comment to set
     */
    public void setComments(String comment) {
        this.comments = comment;
    }
    
    /**
     * @return the categoryID
     */
    public int getCategoryID() {
        return categoryID;
    }
    
    /**
     * @param categoryID the categoryID to set
     */
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
    
    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }
    
    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
}
