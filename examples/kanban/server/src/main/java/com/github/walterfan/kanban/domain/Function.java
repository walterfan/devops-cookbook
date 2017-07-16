package com.github.walterfan.kanban.domain;


public class Function extends Operation {
    private int functionID;
    private String functionName;
    private String description;
    
    /**
     * @return the functionID
     */
    public int getFunctionID() {
        return functionID;
    }
    
    /**
     * @param functionID the functionID to set
     */
    public void setFunctionID(int functionID) {
        this.functionID = functionID;
    }
    
    /**
     * @return the functionName
     */
    public String getFunctionName() {
        return functionName;
    }
    
    /**
     * @param functionName the functionName to set
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
