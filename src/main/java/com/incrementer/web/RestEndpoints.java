package com.incrementer.web;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incrementer.data.InterfaceImplementation;
import com.incrementer.dto.AnalyticsObject;

@RestController
public class RestEndpoints {
    
    private final Logger log = LoggerFactory.getLogger(RestEndpoints.class);

    @Autowired
    InterfaceImplementation interfaceImplementation;

    private volatile HashMap<Integer, String> simpleCache = new HashMap<>();

    /**
     * @param number
     * @return
     * @throws URISyntaxException
     * Call to add a number to list
     */
    @PostMapping("/add/{number}")
    public ResponseEntity<Void> addNumber(@PathVariable String number) throws URISyntaxException {
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(number);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        this.interfaceImplementation.add(decimal);
        simpleCache = new HashMap<>();
        return ResponseEntity.ok().build();
    }

    /**
     * @param number
     * @return
     * @throws URISyntaxException
     * Call to start tracking a moving average. The below moving average call will still return the corresponding moving average but we might want to
     * 'track' it so reads can occur quicker. Without tracking the moving average will be calculated on the spot.
     */
    @PostMapping("/start-tracking/{number}")
    public ResponseEntity<String> startTracking(@PathVariable Integer number) throws URISyntaxException {
        String returnValue = this.interfaceImplementation.startTracking(number).toString();
        this.simpleCache.put(number, returnValue);
        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

    /**
     * @param number
     * @return
     * @throws URISyntaxException
     * Removes a number from the set of moving averages being tracked.
     */
    @DeleteMapping("/stop-tracking/{number}")
    public ResponseEntity<Void> stopTracking(@PathVariable Integer number) throws URISyntaxException {
        this.interfaceImplementation.removeTracking(number);
        return ResponseEntity.ok().build();
    }

    /**
     * @param number
     * @return
     * @throws URISyntaxException
     * Returns the moving average in the list. If the moving average is being tracked will return quickly. Otherwise will calculate the number on the spot.
     * Probably not a big deal until the list is long enough.
     */
    @GetMapping("/get-moving-average/{number}")
    public ResponseEntity<String> getMovingAverage(@PathVariable Integer number) throws URISyntaxException {
        if (this.simpleCache.containsKey(number)) {
            return new ResponseEntity<>(this.simpleCache.get(number), HttpStatus.OK);
        } else {
            String returnValue = this.interfaceImplementation.getMovingAverage(number).toString();
            this.simpleCache.put(number, returnValue);
            return new ResponseEntity<>(returnValue, HttpStatus.OK);
        }
    }

    /**
     * @param from
     * @param to
     * @return
     * @throws URISyntaxException
     * Honestly not sure what this should return so just return number and frequency of calls within a time period. It can be extended but the requirements
     * are pretty vague.
     */
    @GetMapping("/get-analytics")
    public ResponseEntity<AnalyticsObject> getAnalytics(@RequestParam(value = "from") @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm") Date from,
          @RequestParam(value = "to") @DateTimeFormat(pattern = "yyyy-MM-dd-HH:mm") Date to) throws URISyntaxException {
        if (to.compareTo(from) <= 0) {
            return ResponseEntity.badRequest().build();
        } else {
            return new ResponseEntity<>(this.interfaceImplementation.getAnalytics(from, to), HttpStatus.OK);
        }
    }

    /**
     * @return
     * @throws URISyntaxException
     * Backups up the data and reads it back in. Basically resulting in it being 'reprocessed' if there was something wrong with an algorithm
     */
    @PostMapping("/reprocess-data")
    public ResponseEntity<Void> reprocessData() throws URISyntaxException {
        log.info("Beginning to reprocess data");

        this.interfaceImplementation.reprocessData();

        log.info("Finished reprocessing data");
        return ResponseEntity.ok().build();
    }

    /**
     * @return
     * @throws URISyntaxException
     * Clears all data in memory.
     */
    @DeleteMapping("/clear-data")
    public ResponseEntity<Void> clearData() throws URISyntaxException {
        log.debug("Beginning to reprocess data");

        this.interfaceImplementation.clearData();

        log.debug("Finished to reprocessing data");
        return ResponseEntity.ok().build();
    }
}
