package com.debugflow.core.exporter;

import com.debugflow.core.event.TraceEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleJsonExporter implements Exporter {
    private static final Logger log = LoggerFactory.getLogger(ConsoleJsonExporter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void export(TraceEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("[DebugFlow] {}", json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize TraceEvent: {}", e.getMessage());
        }
    }
}

