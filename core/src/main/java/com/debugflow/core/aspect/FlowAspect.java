package com.debugflow.core.aspect;

import com.debugflow.core.event.EventBus;
import com.debugflow.core.event.TraceEvent;
import com.debugflow.core.session.SessionManager;
import com.debugflow.core.web.TraceContextFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
public class FlowAspect {
    private final SessionManager sessionManager;
    private final EventBus eventBus;
    private final String serviceName;
    private final boolean followInboundTraces;
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<java.util.Deque<String>> SPAN_STACK = ThreadLocal.withInitial(java.util.ArrayDeque::new);

    public FlowAspect(SessionManager sessionManager, EventBus eventBus, String serviceName, boolean followInboundTraces) {
        this.sessionManager = sessionManager;
        this.eventBus = eventBus;
        this.serviceName = serviceName;
        this.followInboundTraces = followInboundTraces;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String existingTrace = MDC.get(TraceContextFilter.TRACE_ID_KEY);
        boolean enabled = sessionManager.isActive() || (followInboundTraces && existingTrace != null && !existingTrace.isEmpty());
        if (!enabled) { return pjp.proceed(); }
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        String op = className + "#" + methodName;
        String traceId = existingTrace != null ? existingTrace : "";

        // Initialize from MDC for async threads if needed
        java.util.Deque<String> stack = SPAN_STACK.get();
        int d = DEPTH.get();
        String mdcDepth = MDC.get("df.depth");
        String mdcSpan = MDC.get("df.span");
        if (stack.isEmpty() && mdcDepth != null) {
            try { d = Integer.parseInt(mdcDepth) + 1; } catch (NumberFormatException ignored) {}
            DEPTH.set(d);
        }

        // parentId is from top of stack if present, else from MDC span
        String parentId = stack.peek();
        if (parentId == null) parentId = mdcSpan;
        String spanId = com.debugflow.core.util.TraceIds.randomSpanId();

        // START event at current depth
        TraceEvent start = TraceEvent.flowStart(traceId, serviceName, op, d);
        start.setSpanId(spanId);
        start.setParentId(parentId);
        start.setThread(Thread.currentThread().getName());

        // If this is a controller, add HTTP method/path
        if (isController(pjp)) {
            HttpServletRequest req = currentRequest();
            if (req != null) {
                start.setHttpMethod(req.getMethod());
                start.setHttpPath(req.getRequestURI());
            }
        }
        eventBus.publish(start);

        // Update context for children
        MDC.put("df.depth", Integer.toString(d));
        MDC.put("df.span", spanId);
        stack.push(spanId);
        DEPTH.set(d + 1);

        long t0 = System.nanoTime();
        boolean error = false;
        Throwable caught = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            error = true;
            caught = t;
            throw t;
        } finally {
            long ns = System.nanoTime() - t0;
            long durMs = ns / 1_000_000;
            // END event at original depth
            TraceEvent ev = TraceEvent.flowEnd(traceId, serviceName, op, durMs, d);
            ev.setDurNs(ns);
            ev.setSpanId(spanId);
            ev.setParentId(parentId);
            ev.setThread(Thread.currentThread().getName());
            if (isController(pjp)) {
                HttpServletResponse resp = currentResponse();
                if (resp != null) ev.setHttpStatus(resp.getStatus());
            }
            if (error) {
                ev.setError(true);
                ev.setErrorType(caught.getClass().getSimpleName());
                String msg = caught.getMessage();
                if (msg != null && msg.length() > 120) msg = msg.substring(0, 120) + "...";
                ev.setErrorMsg(msg);
            }
            eventBus.publish(ev);
            // pop and restore depth
            stack.poll();
            DEPTH.set(d);
            // restore MDC to parent
            String parent = stack.peek();
            if (parent != null) MDC.put("df.span", parent); else MDC.remove("df.span");
            MDC.put("df.depth", Integer.toString(Math.max(0, d - 1)));
        }
    }

    private boolean isController(ProceedingJoinPoint pjp) {
        Class<?> cls = pjp.getTarget() != null ? pjp.getTarget().getClass() : null;
        return cls != null && cls.isAnnotationPresent(RestController.class);
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) return sra.getRequest();
        return null;
    }

    private HttpServletResponse currentResponse() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) return sra.getResponse();
        return null;
    }
}
