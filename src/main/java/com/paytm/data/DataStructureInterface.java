package com.paytm.data;

import java.math.BigDecimal;
import java.util.Date;

import com.paytm.dto.AnalyticsObject;

public interface DataStructureInterface {
    /**
     * @param number
     * Adds number to list
     */
    public void add(BigDecimal number);

    /**
     * @param number
     * @return BigDecimal
     * Returns moving average of size 'number'
     */
    public BigDecimal getMovingAverage(Integer number);

    /**
     * @param from
     * @param to
     * @return AnalysticsObject
     * return object which for now only has two variables
     */
    public AnalyticsObject getAnalytics(Date from, Date to);

    /**
     * Clears list and start anew
     */
    public void clearData();
    
}
