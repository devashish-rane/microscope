package com.debugflow.core.web;

import com.debugflow.core.util.TraceIds;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceContextFilter extends OncePerRequestFilter {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACEPARENT = "traceparent";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String incoming = request.getHeader(TRACEPARENT);
        String traceId;
        if (incoming != null && incoming.length() >= 55) {
            // very lax parse of W3C traceparent: 00-<traceId>-<spanId>-flags
            String[] parts = incoming.split("-");
            traceId = parts.length >= 3 ? parts[1] : TraceIds.randomTraceId();
        } else {
            traceId = TraceIds.randomTraceId();
        }
        MDC.put(TRACE_ID_KEY, traceId);
        try {
            // Echo traceparent so clients can correlate
            String spanId = TraceIds.randomSpanId();
            response.setHeader(TRACEPARENT, "00-" + traceId + "-" + spanId + "-01");
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}

