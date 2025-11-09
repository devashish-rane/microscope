package com.debugflow.core.aspect;

import com.debugflow.core.event.EventBus;
import com.debugflow.core.event.TraceEvent;
import com.debugflow.core.session.SessionManager;
import com.debugflow.core.web.TraceContextFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

@Aspect
public class FlowAspect {
    private final SessionManager sessionManager;
    private final EventBus eventBus;
    private final String serviceName;
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public FlowAspect(SessionManager sessionManager, EventBus eventBus, String serviceName) {
        this.sessionManager = sessionManager;
        this.eventBus = eventBus;
        this.serviceName = serviceName;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        if (!sessionManager.isActive()) { return pjp.proceed(); }
        String op = pjp.getSignature().getDeclaringTypeName() + "#" + pjp.getSignature().getName();
        String traceId = MDC.get(TraceContextFilter.TRACE_ID_KEY);
        if (traceId == null) traceId = "";

        int d = DEPTH.get();
        // START event at current depth
        eventBus.publish(TraceEvent.flowStart(traceId, serviceName, op, d));
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
            long durMs = (System.nanoTime() - t0) / 1_000_000;
            // END event at original depth
            TraceEvent ev = TraceEvent.flowEnd(traceId, serviceName, op, durMs, d);
            ev.setThread(Thread.currentThread().getName());
            if (error) {
                ev.setError(true);
                ev.setErrorType(caught.getClass().getSimpleName());
                String msg = caught.getMessage();
                if (msg != null && msg.length() > 120) msg = msg.substring(0, 120) + "...";
                ev.setErrorMsg(msg);
            }
            eventBus.publish(ev);
            DEPTH.set(d);
        }
    }
}
