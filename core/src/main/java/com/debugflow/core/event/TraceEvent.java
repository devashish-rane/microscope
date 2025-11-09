package com.debugflow.core.event;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraceEvent {
    private String type; // e.g., FLOW, SQL
    private String traceId;
    private String spanId;
    private String parentId;
    private String service;
    private String op;
    private Long durMs;
    private Long durNs;
    private String phase; // START | END (for FLOW)
    private Integer depth; // call depth for pretty formatting
    private Boolean error;
    private String errorType;
    private String errorMsg;
    private String thread;
    private String httpMethod;
    private String httpPath;
    private Integer httpStatus;

    public static TraceEvent flowEnd(String traceId, String service, String op, long durMs, int depth) {
        TraceEvent e = new TraceEvent();
        e.type = "FLOW";
        e.traceId = traceId;
        e.service = service;
        e.op = op;
        e.durMs = durMs;
        e.phase = "END";
        e.depth = depth;
        return e;
    }

    public static TraceEvent flowStart(String traceId, String service, String op, int depth) {
        TraceEvent e = new TraceEvent();
        e.type = "FLOW";
        e.traceId = traceId;
        e.service = service;
        e.op = op;
        e.phase = "START";
        e.depth = depth;
        return e;
    }

    public String getType() { return type; }
    public String getTraceId() { return traceId; }
    public String getSpanId() { return spanId; }
    public String getParentId() { return parentId; }
    public String getService() { return service; }
    public String getOp() { return op; }
    public Long getDurMs() { return durMs; }

    public void setType(String type) { this.type = type; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setService(String service) { this.service = service; }
    public void setOp(String op) { this.op = op; }
    public void setDurMs(Long durMs) { this.durMs = durMs; }
    public Long getDurNs() { return durNs; }
    public void setDurNs(Long durNs) { this.durNs = durNs; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Integer getDepth() { return depth; }
    public void setDepth(Integer depth) { this.depth = depth; }
    public Boolean getError() { return error; }
    public void setError(Boolean error) { this.error = error; }
    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getThread() { return thread; }
    public void setThread(String thread) { this.thread = thread; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getHttpPath() { return httpPath; }
    public void setHttpPath(String httpPath) { this.httpPath = httpPath; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
}
