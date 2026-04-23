package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    // Returns all historical readings for this sensor
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        // Check sensor exists
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException(
                "Sensor not found with ID: " + sensorId
            );
        }

        List<SensorReading> list = DataStore.readings
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(list).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    // Adds a new reading for this sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Step 1 — Check the sensor exists
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException(
                "Sensor not found with ID: " + sensorId
            );
        }

        // Step 2 — Check sensor is not in MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + 
                "' is currently under MAINTENANCE and cannot accept new readings."
            );
        }

        // Step 3 — Auto-generate ID and timestamp
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // Step 4 — Save the reading to the readings map
        DataStore.readings
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // Step 5 — SIDE EFFECT: update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).entity(reading).build();
    }
}