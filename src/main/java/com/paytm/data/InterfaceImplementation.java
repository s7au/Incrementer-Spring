package com.paytm.data;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.paytm.dto.AnalyticsObject;

@Component
public class InterfaceImplementation implements DataStructureInterface {

    private volatile NavigableMap<Date, BigDecimal> navigableMap;
    private volatile ArrayList<TimeStampNumber> numberList;
    private volatile Map<Integer, BigDecimal> trackList;

    public InterfaceImplementation() {
        navigableMap = new TreeMap<>(); //lol
        numberList = new ArrayList<>();
        trackList = new HashMap<>();
        this.recover();
    }

    @Override
    public synchronized void add(BigDecimal number) {
        Date timeStamp = new Date();
        TimeStampNumber entry = new TimeStampNumber(timeStamp, number);
        synchronized (trackList) {
            navigableMap.put(timeStamp, number);
            numberList.add(entry);
            int size = numberList.size();
            for (Map.Entry<Integer, BigDecimal> mapEntry : trackList.entrySet()) {
                BigDecimal value = mapEntry.getValue().add(number);
                if (mapEntry.getKey().compareTo(size) < 0) {
                    value = value.subtract(numberList.get(size - mapEntry.getKey() - 1).number);
                }
                trackList.put(mapEntry.getKey(), value);
            }
        }
    }

    /* opting to recalculate because there isn't actually a way to scale with the assumption that there are more writes than reads
     * (this is based on the original question) If we are planning to do more reads than writes we'd keep track of sums instead of
     * the numbers but this would involve adding the newly added number to every index of a list. 
     * Also basing this on the assumption that the moving average returned has to be of variable size.
     */
    @Override
    public BigDecimal getMovingAverage(Integer number) {
        int size = numberList.size();
        if (number > size) number = size;
        if (size == 0) { // divide by zero exception
            return BigDecimal.ZERO;
        }
        if (trackList.containsKey(number)) {
            return trackList.get(number).divide(new BigDecimal(number), 8, RoundingMode.HALF_UP);
        }
        BigDecimal accum = BigDecimal.ZERO;
        for (int i = size - number; i < size; i++) {
            accum = accum.add(numberList.get(i).number);
        }
        return accum.divide(new BigDecimal(number), 8, RoundingMode.HALF_UP); // this is pretty arbitrary but we can't return non-terminating numbers anyway
    }

    // process a word document probably
    @SuppressWarnings("deprecation")
    private void recover() {
        File file = new File("backup.txt");
        CharSource source = Files.asCharSource(file, Charsets.UTF_8);
        
        String[] result = {};
        try {
            result = source.read().split(",");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int length = result.length;
        if (length <= 1) return; // deal with weird issue when backup file is empty
        for (int i = 0; i < length; i += 2) {
            Date timeStamp = new Date(result[i+1]); // really shouldn't be using Date obviously
            BigDecimal number = new BigDecimal(result[i]);
            TimeStampNumber entry = new TimeStampNumber(timeStamp, number);
            synchronized (this) {
                navigableMap.put(timeStamp, number);
                numberList.add(entry);
            }
        }
    }

    // output data probably text document
    public void outputData() {
        List<String> outputList = new LinkedList<>();
        for (TimeStampNumber entry : numberList) {
            outputList.add(entry.number.toString());
            outputList.add(entry.timeStamp.toString());
        }
        File file = new File("backup.txt");
        CharSink sink = Files.asCharSink(file, Charsets.UTF_8);
        try {
            sink.writeLines(outputList, ",");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearData() {
        navigableMap = new TreeMap<>();
        numberList = new ArrayList<>();
        trackList = new HashMap<>();
    }

    public void reprocessData() {
        this.outputData();
        this.clearData();
        this.recover();
    }

    @Override
    public AnalyticsObject getAnalytics(Date from, Date to) {
        SortedMap<Date, BigDecimal> blah = this.navigableMap.subMap(from, to);
        AnalyticsObject analyticsObject = new AnalyticsObject();
        analyticsObject.totalWrites = String.valueOf(blah.size());
        analyticsObject.frequencyOfWrites = String.valueOf(new BigDecimal(blah.size()).divide(new BigDecimal((to.getTime() - from.getTime())/1000), 8, RoundingMode.HALF_UP));
        return analyticsObject;
    }

    public void removeTracking(Integer number) {
        synchronized (trackList) {
            this.trackList.remove(number);
        }
    }

    public BigDecimal startTracking(Integer number) {
        synchronized (trackList) {
            this.trackList.put(number, this.getMovingAverage(number));
        }
        return this.trackList.get(number);
    }
}

class TimeStampNumber {
    public Date timeStamp;
    public BigDecimal number;
    public TimeStampNumber() { }
    public TimeStampNumber(Date timeStamp, BigDecimal number) {
        this.timeStamp = timeStamp;
        this.number = number;
    }
}