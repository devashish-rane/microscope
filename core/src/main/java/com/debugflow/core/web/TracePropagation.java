package com.debugflow.core.web;

import com.debugflow.core.event.EventBus;
import com.debugflow.core.event.TraceEvent;
import com.debugflow.core.util.TraceIds;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

public class TracePropagation implements RestTemplateCustomizer, ClientHttpRequestInterceptor {
    private final EventBus eventBus;
    private final String serviceName;

    public TracePropagation(EventBus eventBus, String serviceName) {
        this.eventBus = eventBus;
        this.serviceName = serviceName;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(this);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get(TraceContextFilter.TRACE_ID_KEY);
        String currentSpan = MDC.get("df.span");
        int depth = 0;
        String d = MDC.get("df.depth");
        if (d != null) {
            try { depth = Integer.parseInt(d) + 1; } catch (NumberFormatException ignored) {}
        }
        // Propagate W3C
        if (traceId != null && !traceId.isEmpty()) {
            String spanId = TraceIds.randomSpanId();
            String tp = "00-" + traceId + "-" + spanId + "-01";
            request.getHeaders().set(TraceContextFilter.TRACEPARENT, tp);
        }

        // Emit FLOW for HTTP client call
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";
        URI uri = request.getURI();
        String path = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        String op = "HTTP " + method + " " + path;
        String spanId = TraceIds.randomSpanId();
        if (traceId != null) {
            TraceEvent start = TraceEvent.flowStart(traceId, serviceName, op, depth);
            start.setSpanId(spanId);
            start.setParentId(currentSpan);
            start.setThread(Thread.currentThread().getName());
            start.setHttpMethod(method);
            start.setHttpPath(path);
            eventBus.publish(start);
        }

        long t0 = System.nanoTime();
        boolean error = false;
        int status = -1;
        try {
            ClientHttpResponse response = execution.execute(request, body);
            try { status = response.getRawStatusCode(); } catch (Throwable ignored) {}
            return response;
        } catch (IOException ioe) {
            error = true;
            throw ioe;
        } finally {
            if (traceId != null) {
                long ns = System.nanoTime() - t0;
                long ms = ns / 1_000_000;
                TraceEvent end = TraceEvent.flowEnd(traceId, serviceName, op, ms, depth);
                end.setDurNs(ns);
                end.setSpanId(spanId);
                end.setParentId(currentSpan);
                end.setThread(Thread.currentThread().getName());
                end.setHttpStatus(status > 0 ? status : null);
                if (error) end.setError(true);
                eventBus.publish(end);
            }
        }
    }
}
