package com.smarthome.webapp.aggregation.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthome.webapp.aggregation.repository.DailyReadingDocument;
import com.smarthome.webapp.aggregation.repository.DailyReadingRepository;
import com.smarthome.webapp.aggregation.repository.HourlyReadingDocument;
import com.smarthome.webapp.aggregation.repository.HourlyReadingRepository;
import com.smarthome.webapp.aggregation.repository.MonthlyReadingDocument;
import com.smarthome.webapp.aggregation.repository.MonthlyReadingRepository;
import com.smarthome.webapp.aggregation.repository.YearlyReadingDocument;
import com.smarthome.webapp.aggregation.repository.YearlyReadingRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    public AggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Ensure proper indexes exist on startup to speed queries
    @PostConstruct
    public void ensureIndexes() {
        createIndexIfMissing("device_readings_hourly");
        createIndexIfMissing("device_readings_daily");
        createIndexIfMissing("device_readings_monthly");
        createIndexIfMissing("device_readings_yearly");
    }

    private void createIndexIfMissing(String collection) {
        IndexOperations ops = mongoTemplate.indexOps(collection);
        Index idx = new Index()
                .on("device", Sort.Direction.ASC)
                .on("sensor", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.ASC);
        ops.ensureIndex(idx);
    }

    @Scheduled(cron = "0 5 * * * *")  // every hour at :05
    public void hourlyAggregation() {
        aggregateHourly();
    }

    @Scheduled(cron = "0 10 0 * * *") // daily at 00:10
    public void dailyAggregation() {
        aggregateDaily();
    }

    @Scheduled(cron = "0 15 0 1 * *") // monthly at 00:15 on 1st
    public void monthlyAggregation() {
        aggregateMonthly();
    }

    @Scheduled(cron = "0 20 0 1 1 *") // yearly at 00:20 Jan 1st
    public void yearlyAggregation() {
        aggregateYearly();
    }

    /**
     * Aggregate raw readings for the previous completed hour into hourly collection.
     * Only scans raw data for that hour.
     */
    public void aggregateHourly() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime lastHourStart = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);
        ZonedDateTime lastHourEnd = lastHourStart.plusHours(1);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp")
                        .gte(Date.from(lastHourStart.toInstant()))
                        .lt(Date.from(lastHourEnd.toInstant()))),

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

                Aggregation.match(Criteria.where("numericValue").ne(null)),

                Aggregation.project()
                        .and("metadata.device").as("device")
                        .and("metadata.name").as("sensor")
                        .andExpression("{ $dateTrunc: { date: '$timestamp', unit: 'hour' } }").as("interval")
                        .and("numericValue").as("numericValue"),

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
            upsertDocuments(results, "device_readings_hourly");
        }
    }

    /**
     * Aggregate hourly -> daily for the previous completed day (yesterday).
     * Only scans hourly docs for the day to keep aggregation small.
     */
    public void aggregateDaily() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startOfYesterday = now.minusDays(1);
        ZonedDateTime startOfToday = now;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp")
                        .gte(Date.from(startOfYesterday.toInstant()))
                        .lt(Date.from(startOfToday.toInstant()))),

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
            upsertDocuments(results, "device_readings_daily");
        }
    }

    /**
     * Aggregate daily -> monthly for the previous completed month.
     * Only scans daily docs for last month.
     */
    public void aggregateMonthly() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        // last month start and end
        ZonedDateTime lastMonthStart = now.minusMonths(1);
        ZonedDateTime lastMonthEnd = now;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp")
                        .gte(Date.from(lastMonthStart.toInstant()))
                        .lt(Date.from(lastMonthEnd.toInstant()))),

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
            upsertDocuments(results, "device_readings_monthly");
        }
    }

    /**
     * Aggregate monthly -> yearly for the previous completed year.
     * Only scans monthly docs for last year.
     */
    public void aggregateYearly() {
        // We treat "last year" as the previous calendar year (Jan 1..Dec 31)
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime lastYearStart = now.minusYears(1);
        ZonedDateTime lastYearEnd = now;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp")
                        .gte(Date.from(lastYearStart.toInstant()))
                        .lt(Date.from(lastYearEnd.toInstant()))),

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

        List<Document> results = mongoTemplate.aggregate(aggregation, "device_readings_monthly", Document.class)
                .getMappedResults();

        if (!results.isEmpty()) {
            upsertDocuments(results, "device_readings_yearly");
        }
    }

    // -------------------------
    // Helper: upsert aggregated docs (prevents duplicates)
    // -------------------------
    private void upsertDocuments(List<Document> docs, String targetCollection) {
        // NOTE: this is implemented as a simple per-document upsert using mongoTemplate.upsert.
        // For very large result sets consider using BulkOperations.upsert for performance.
        for (Document doc : docs) {
            Object device = doc.get("device");
            Object sensor = doc.get("sensor");
            Object timestamp = doc.get("timestamp");

            Query q = Query.query(Criteria.where("device").is(device)
                    .and("sensor").is(sensor)
                    .and("timestamp").is(timestamp));

            // Build Update from the document - set the fields explicitly
            Update u = new Update();
            if (doc.containsKey("avg")) u.set("avg", doc.get("avg"));
            if (doc.containsKey("min")) u.set("min", doc.get("min"));
            if (doc.containsKey("max")) u.set("max", doc.get("max"));
            if (doc.containsKey("count")) u.set("count", doc.get("count"));
            u.set("device", device);
            u.set("sensor", sensor);
            u.set("timestamp", timestamp);

            mongoTemplate.upsert(q, u, targetCollection);
        }
    }

    // ---- Hourly for a single day ----
    public ResponseEntity<String> getHourlyForDay(String device, String sensor, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
        try {
            List<HourlyReadingDocument> deviceReadings = hourlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
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
            List<DailyReadingDocument> deviceReadings = dailyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
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
            List<MonthlyReadingDocument> deviceReadings = monthlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);
    
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
            List<YearlyReadingDocument> deviceReadings = yearlyRepo.findByDeviceAndSensorAndTimestampBetween(device, sensor, start, end);

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
