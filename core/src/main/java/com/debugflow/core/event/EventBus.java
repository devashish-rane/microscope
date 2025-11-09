package com.debugflow.core.event;

import com.debugflow.core.exporter.Exporter;

import java.util.List;

public class EventBus {
    private final List<Exporter> exporters;

    public EventBus(List<Exporter> exporters) {
        this.exporters = exporters;
    }

    public void publish(TraceEvent event) {
        for (Exporter e : exporters) {
            try { e.export(event); } catch (RuntimeException ex) { /* best effort */ }
        }
    }
}

