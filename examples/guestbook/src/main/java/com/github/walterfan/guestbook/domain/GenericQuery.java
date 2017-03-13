package com.github.walterfan.guestbook.domain;

/**
 * Created by walter on 06/11/2016.
 */


public class GenericQuery {

    public enum OrderType {NONE, ASC, DESC} ;

    private int limit;

    private int start;

    private OrderType order;

    private String sortBy;

    private String keyword;

    private String fieldName;

    public GenericQuery() {
        start = 0;
        limit = 20;
        order = OrderType.NONE;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public OrderType getOrder() {
        return order;
    }

    public void setOrder(OrderType order) {
        this.order = order;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
