package com.example.orders.sql;

import com.debugflow.core.event.EventBus;
import com.debugflow.core.event.SqlEvent;
import com.debugflow.core.web.TraceContextFilter;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlCaptureTemplate {
    private final JdbcTemplate jdbc;
    private final EventBus eventBus;
    private final String serviceName;

    public SqlCaptureTemplate(JdbcTemplate jdbc, EventBus eventBus, String serviceName) {
        this.jdbc = jdbc;
        this.eventBus = eventBus;
        this.serviceName = serviceName;
    }

    public int update(String sql, Object... args) {
        long t0 = System.nanoTime();
        int out = jdbc.update(sql, args);
        publish(sql, t0, out);
        return out;
    }

    public <T> T query(String sql, org.springframework.jdbc.core.ResultSetExtractor<T> rse, Object... args) {
        long t0 = System.nanoTime();
        T out = jdbc.query(sql, rse, args);
        publish(sql, t0, null);
        return out;
    }

    public <T> java.util.List<T> query(String sql, org.springframework.jdbc.core.RowMapper<T> rowMapper, Object... args) {
        long t0 = System.nanoTime();
        java.util.List<T> out = jdbc.query(sql, rowMapper, args);
        publish(sql, t0, out != null ? out.size() : null);
        return out;
    }

    private void publish(String sql, long startNs, Integer rows) {
        long ms = (System.nanoTime() - startNs) / 1_000_000;
        String traceId = MDC.get(TraceContextFilter.TRACE_ID_KEY);
        int depth = 0;
        String d = MDC.get("df.depth");
        if (d != null) try { depth = Integer.parseInt(d) + 1; } catch (NumberFormatException ignored) {}
        eventBus.publish(SqlEvent.sql(traceId, serviceName, sql, ms, depth, rows));
    }
}

