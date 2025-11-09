package com.debugflow.core.exporter;

import com.debugflow.core.event.TraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrettyConsoleExporter implements Exporter {
    private static final Logger log = LoggerFactory.getLogger("DebugFlowPretty");
    private final boolean color;

    public PrettyConsoleExporter(boolean color) {
        this.color = color;
    }

    @Override
    public void export(TraceEvent e) {
        if ("SQL".equals(e.getType())) {
            int depth = e.getDepth() != null ? e.getDepth() : 0;
            String indent = "  ".repeat(Math.max(0, depth));
            String tid = paint(shortId(e.getTraceId()), ANSI.MAGENTA);
            String svc = paint(e.getService(), ANSI.CYAN);
            String sql = e instanceof com.debugflow.core.event.SqlEvent se ? se.getSql() : null;
            String snippet = sql == null ? "SQL" : truncate(sql, 80);
            String durStr = colorizeDuration(durationMsOrMicros(e));
            log.info(String.format("%s• [%s] %s %s %s", indent, tid, svc, paint(snippet, ANSI.BLUE), durStr));
            return;
        }
        if (!"FLOW".equals(e.getType())) return; // only FLOW
        int depth = e.getDepth() != null ? e.getDepth() : 0;
        String indent = "  ".repeat(Math.max(0, depth));
        String svc = paint(e.getService(), ANSI.CYAN);
        String op = paint(e.getOp(), ANSI.WHITE);
        String tid = paint(shortId(e.getTraceId()), ANSI.MAGENTA);
        if ("START".equals(e.getPhase())) {
            String line = String.format("%s→ [%s] %s %s", indent, tid, svc, op);
            log.info(line);
        } else if ("END".equals(e.getPhase())) {
            String durStr = colorizeDuration(durationMsOrMicros(e));
            String line = String.format("%s← [%s] %s %s %s", indent, tid, svc, op, durStr);
            log.info(line);
        }
    }

    private long durationMsOrMicros(TraceEvent e) {
        if (e.getDurMs() != null && e.getDurMs() > 0) return e.getDurMs();
        if (e.getDurNs() != null) return Math.max(1, e.getDurNs() / 1000); // treat as µs scale for coloring
        return -1;
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n - 1) + "…" : s;
    }

    private String shortId(String id) {
        if (id == null) return "-";
        return id.length() > 8 ? id.substring(0, 8) : id;
    }

    private String colorizeDuration(long durMs) {
        if (durMs < 0) return paint("? ms", ANSI.WHITE);
        String text = durMs + "ms";
        if (durMs < 10) return paint(text, ANSI.GREEN);
        if (durMs < 100) return paint(text, ANSI.YELLOW);
        return paint(text, ANSI.RED);
    }

    private String paint(String s, String code) {
        if (!color) return s;
        return code + s + ANSI.RESET;
    }

    private static class ANSI {
        static final String RESET = "\u001B[0m";
        static final String RED = "\u001B[31m";
        static final String GREEN = "\u001B[32m";
        static final String YELLOW = "\u001B[33m";
        static final String BLUE = "\u001B[34m";
        static final String MAGENTA = "\u001B[35m";
        static final String CYAN = "\u001B[36m";
        static final String WHITE = "\u001B[37m";
    }
}
