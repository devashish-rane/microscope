package com.debugflow.core.exporter;

import com.debugflow.core.event.TraceEvent;

public interface Exporter {
    void export(TraceEvent event);
    default void flush() {}
}

