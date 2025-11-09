package com.debugflow.core.event;

public class SqlEvent extends TraceEvent {
    private String sql;
    private Integer rows;

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public static SqlEvent sql(String traceId, String service, String sql, long timeMs, int depth, Integer rows) {
        SqlEvent e = new SqlEvent();
        e.setType("SQL");
        e.setTraceId(traceId);
        e.setService(service);
        e.setDurMs(timeMs);
        e.setDepth(depth);
        e.setSql(sql);
        e.setRows(rows);
        return e;
    }
}

