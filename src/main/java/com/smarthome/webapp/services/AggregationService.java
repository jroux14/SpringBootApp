package com.smarthome.webapp.services;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthome.webapp.objects.DailyReading;
import com.smarthome.webapp.objects.HourlyReading;
import com.smarthome.webapp.objects.MonthlyReading;
import com.smarthome.webapp.objects.YearlyReading;
import com.smarthome.webapp.repositories.DailyReadingRepository;
import com.smarthome.webapp.repositories.HourlyReadingRepository;
import com.smarthome.webapp.repositories.MonthlyReadingRepository;
import com.smarthome.webapp.repositories.YearlyReadingRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class AggregationService {

    @Autowired
    private HourlyReadingRepository hourlyRepo;
    @Autowired
    private DailyReadingRepository dailyRepo;
    @Autowired
    private MonthlyReadingRepository monthlyRepo;
    @Autowired
    private YearlyReadingRepository yearlyRepo;

    private final MongoTemplate mongoTemplate;

    public AggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Cron job running daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateOldData() {
        System.out.println("AGGREGATION SERVICE STARTED AT: " + new Date());
        aggregateHourly();
        aggregateDaily();
        aggregateMonthly();
        aggregateYearly();
    }

    /** Aggregate raw entries older than 1 year into hourly summaries */
    public void aggregateHourly() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusYears(1);
        // Uncomment for debug
        // ZonedDateTime cutoffTime = ZonedDateTime.now().minusMinutes(1);
        Date cutoffDate = Date.from(cutoffTime.toInstant());

        Aggregation aggregation = Aggregation.newAggregation(
            // Match entries older than cutoff
            Aggregation.match(Criteria.where("timestamp").lt(cutoffDate)),
        
            // Add numericValue safely using $convert with onError and onNull
            Aggregation.addFields()
                .addFieldWithValue("numericValue",
                    new Document("$convert", 
                        new Document("input", "$value")
                            .append("to", "double")
                            .append("onError", null)
                            .append("onNull", null)
                    )
                )
                .build(),
        
            // Keep only successfully converted numeric values
            Aggregation.match(Criteria.where("numericValue").ne(null)),
        
            // Compute truncated hour interval
            Aggregation.project()
                .and("metadata.device").as("device")
                .and("metadata.name").as("sensor")
                .andExpression("{ $dateTrunc: { date: '$timestamp', unit: 'hour' } }").as("interval")
                .and("numericValue").as("numericValue"),
        
            // Group by device, sensor, interval
            Aggregation.group("device", "sensor", "interval")
                .avg("numericValue").as("avg")
                .min("numericValue").as("min")
                .max("numericValue").as("max")
                .count().as("count"),
        
            Aggregation.project()
                .and("_id.device").as("device")
                .and("_id.sensor").as("sensor")
                .and("_id.interval").as("timestamp")
                .andInclude("avg", "min", "max", "count")
                .andExclude("_id")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, "device_readings", Document.class)
                .getMappedResults();

        if (!results.isEmpty()) {
            mongoTemplate.insert(results, "device_readings_hourly");
        }
    }

    /** Aggregate hourly data into daily summaries */
    public void aggregateDaily() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.project()
                .and("device").as("device")
                .and("sensor").as("sensor")
                .andExpression("{ $dateTrunc: { date: '$timestamp', unit: 'day' } }").as("interval")
                .and("avg").as("avg")
                .and("min").as("min")
                .and("max").as("max")
                .and("count").as("count"),

            Aggregation.group("device", "sensor", "interval")
                .avg("avg").as("avg")
                .min("min").as("min")
                .max("max").as("max")
                .sum("count").as("count"),

            Aggregation.project()
                .and("_id.device").as("device")
                .and("_id.sensor").as("sensor")
                .and("_id.interval").as("timestamp")
                .andInclude("avg", "min", "max", "count")
                .andExclude("_id")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, "device_readings_hourly", Document.class)
                .getMappedResults();

        if (!results.isEmpty()) {
            mongoTemplate.insert(results, "device_readings_daily");
        }
    }

    /** Aggregate daily data into monthly summaries */
    public void aggregateMonthly() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.project()
                .and("device").as("device")
                .and("sensor").as("sensor")
                .andExpression("{ $dateTrunc: { date: '$timestamp', unit: 'month' } }").as("interval")
                .and("avg").as("avg")
                .and("min").as("min")
                .and("max").as("max")
                .and("count").as("count"),

            Aggregation.group("device", "sensor", "interval")
                .avg("avg").as("avg")
                .min("min").as("min")
                .max("max").as("max")
                .sum("count").as("count"),

            Aggregation.project()
                .and("_id.device").as("device")
                .and("_id.sensor").as("sensor")
                .and("_id.interval").as("timestamp")
                .andInclude("avg", "min", "max", "count")
                .andExclude("_id")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, "device_readings_daily", Document.class)
                .getMappedResults();

        if (!results.isEmpty()) {
            mongoTemplate.insert(results, "device_readings_monthly");
        }
    }

    /** Aggregate daily data into yearly summaries */
    public void aggregateYearly() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.project()
                .and("device").as("device")
                .and("sensor").as("sensor")
                .andExpression("{ $dateTrunc: { date: '$timestamp', unit: 'year' } }").as("interval")
                .and("avg").as("avg")
                .and("min").as("min")
                .and("max").as("max")
                .and("count").as("count"),

            Aggregation.group("device", "sensor", "interval")
                .avg("avg").as("avg")
                .min("min").as("min")
                .max("max").as("max")
                .sum("count").as("count"),

            Aggregation.project()
                .and("_id.device").as("device")
                .and("_id.sensor").as("sensor")
                .and("_id.interval").as("timestamp")
                .andInclude("avg", "min", "max", "count")
                .andExclude("_id")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, "device_readings_daily", Document.class)
                .getMappedResults();

        if (!results.isEmpty()) {
            mongoTemplate.insert(results, "device_readings_yearly");
        }
    }

    // ---- Hourly for a single day ----
    public ResponseEntity<String> getHourlyForDay(String device, String sensor, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
        try {
            List<HourlyReading> deviceReadings = hourlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
            if (deviceReadings != null) {
                responseBody.put("data", deviceReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseBody.put("error", "Failed to fetch sensor data");
            responseBody.put("success", false);
        }
    
        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // ---- Daily for a month ----
    public ResponseEntity<String> getDailyForMonth(String device, String sensor, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
        try {
            List<DailyReading> deviceReadings = dailyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
            if (deviceReadings != null) {
                responseBody.put("data", deviceReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseBody.put("error", "Failed to fetch sensor data");
            responseBody.put("success", false);
        }
    
        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // ---- Monthly for a year ----
    public ResponseEntity<String> getMonthlyForYear(String device, String sensor, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
        try {
            List<MonthlyReading> deviceReadings = monthlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
            if (deviceReadings != null) {
                responseBody.put("data", deviceReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseBody.put("error", "Failed to fetch sensor data");
            responseBody.put("success", false);
        }
    
        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // ---- Yearly for a range of years ----
    public ResponseEntity<String> getYearlyForRange(String device, String sensor, int startYear, int endYear) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Instant start = LocalDate.of(startYear, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = LocalDate.of(endYear + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

        try {
            List<YearlyReading> deviceReadings = yearlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);

            if (deviceReadings != null) {
                responseBody.put("data", deviceReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Failed to fetch sensor data");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
