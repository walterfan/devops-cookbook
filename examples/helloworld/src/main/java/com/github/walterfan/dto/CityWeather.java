package com.github.walterfan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

/**
 * Created by yafan on 14/10/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityWeather  {
    private int error;

    private String status;

    private Date date;

    private List<WeatherResult> results;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<WeatherResult> getResults() {
        return results;
    }

    public void setResults(List<WeatherResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
