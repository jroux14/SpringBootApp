package com.smarthome.webapp.services;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class AggregationService {

    private final MongoTemplate mongoTemplate;

    public AggregationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Scheduled to run daily at 8:07 PM
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateOldData() {
        System.out.println("AGGREGATION SERVICE STARTED AT: " + new Date());
        aggregateHourly();
        aggregateDaily();
        aggregateYearly();
    }

    /** Aggregate raw entries older than 1 year into hourly summaries */
    public void aggregateHourly() {
        ZonedDateTime cutoffTime = ZonedDateTime.now().minusYears(1);
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
        
            // Final projection
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
}
